import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.async.Async.{async, await}
import akka.pattern.after
import akka.actor.Scheduler
import com.jasonmar.ignite.{CacheBuilder, init}
import com.jasonmar.ignite.sql.sqlQuery
import org.apache.ignite.{Ignite, IgniteCache}
import com.jasonmar.ignite.sql._
import javax.cache.Cache
import org.apache.ignite.cache.affinity.AffinityKey
import org.slf4j.{Logger, LoggerFactory}

import collection.JavaConverters._
import scala.collection.JavaConverters._
import reflect.runtime.universe._
import scala.reflect.ClassTag
import TimingUtils._
import com.jasonmar.ignite.config.IgniteClientConfig

/**
  * Created by evaldas on 18/04/18.
  */
trait IgniteMemCache[KT] {
  val cacheName: String
  implicit val ex: ExecutionContext
  implicit val s: Scheduler
  implicit val ignite: Ignite

  val logger: Logger

  def query[T: TypeTag](sql: String, args: Any*)(implicit tag: ClassTag[T]): Future[Array[Cache.Entry[KT, T]]] = {
    val cache = mkCache[KT, T]()
    async {
      implicit val log = logger
      timed(s"ignite cache fetch for ${cache.getName}.") {
        sqlQuery(cache, sql, args.map(toJavaValueNull): _*).getOrElse(Array())
      }
    }
  }

  def queryFields[T: TypeTag, V](sql: String, args: Any*): Future[List[Option[V]]] = {
    val cache = mkCache[KT, T]()
    async {
      implicit val log = logger
      timed(s"ignite cache agg for ${cache.getName}.") {
        sqlFieldsQuery(cache, sql, iter => {
          iter.map(_.toArray).toList.flatten.map(valueFactory[V](_))
        }, args.map(toJavaValueNull): _*).getOrElse(List())
      }
    }
  }

  def valueFactory[V](source: Any): Option[V] = {
    source match {
      case v: java.math.BigDecimal => Some(BigDecimal(v.asInstanceOf[java.math.BigDecimal]).asInstanceOf[V])
      case v: java.lang.Long       => Some(v.toLong.asInstanceOf[V])
      case null                    => None
      case _                       => Some(source.asInstanceOf[V]) // assume value is converting
    }
  }

  def toJavaValueNull(v: Any): Any = {
    v match {
      case Some(value) => value
      case None        => null
      case _           => v
    }
  }

  def get[T: TypeTag](key: KT): Future[Option[T]] = {
    val cache = mkCache[KT, T]()
    async {
      implicit val log = logger
      timed(s"ignite cache get for ${cache.getName}.") {
        Option(cache.get(key))
      }
    }
  }

  def getAll[T: TypeTag](keys: Set[KT]): Future[Map[KT, T]] = {
    val cache = mkCache[KT, T]()
    async {
      implicit val log = logger
      timed(s"ignite cache getAll for ${cache.getName}.") {
        cache.getAll(keys.asJava).asScala.toMap
      }
    }
  }

  def cleanup(): Future[Unit] = ???

  /**
    * Gets instance of typed cache view to use.
    *
    * @return Cache to use.
    */
  def mkCache[K, V: TypeTag](): IgniteCache[K, V] =
    ignite.cache[K, V](cacheName)

}

object TimingUtils {

  def timed[R](name: String)(block: => R)(implicit logger: Logger): R = {
    val t0     = System.nanoTime()
    val result = block // call-by-name
    val t1     = System.nanoTime()
    logger.debug(s"$name elapsed time: " + (t1 - t0) / 1000000.0 + "ms")
    result
  }
}
