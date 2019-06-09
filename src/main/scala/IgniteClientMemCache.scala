import akka.actor.Scheduler
import com.jasonmar.ignite.sql._
import com.jasonmar.ignite.util.AutoClose
import javax.cache.Cache
import org.apache.ignite.client.{ClientCache, IgniteClient}
import org.apache.ignite.configuration.ClientConfiguration
import org.apache.ignite.{Ignite, Ignition}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.async.Async.async
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe
import scala.util.Try

trait IgniteClientMemCache[KT] {
  private val log: Logger = LoggerFactory.getLogger(this.getClass.getName)

  implicit val cacheName: String
  def server: String
  implicit def ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def loadSeq[V](values: Traversable[V], getKey: V => KT): Option[Unit] = {
    runClient[V, Unit] { cache =>
      log.info(s"pushing seq to cache: ${values.size}")
      values.foreach(v => cache.put(getKey(v), v))
    }
  }

  def loadIter[V, A](values: Iterable[A], transformer: A => V, getKey: V => KT): Option[Unit] = {
    runClient[V, Unit] { cache =>
      log.info(s"${DateTime.now} pushing seq to cache: ${values.size}")
      for (row <- values) {
        val v = transformer(row)
        cache.put(getKey(v), v)
        //        println(s"transform loaded $row")
      }
      log.info(s"${DateTime.now} push finished")
    }
  }

  def getByKey[V](key: KT): Option[V] = {
    runClient[V, V] { cache =>
      cache.get(key)
    }
  }

  def queryFields[V, R](query: String, args: Any*): Option[List[Option[R]]] = {
    runClient[V, List[Option[R]]] { cache =>
      sqlFieldsClientQuery(cache, query, iter => {
        iter.map(_.toArray).toList.flatten.map(valueFactory[R](_))
      }, args.map(toJavaValueNull): _*).getOrElse(List())
    }
  }

  def query[V](query: String, args: Any*)(implicit tag: ClassTag[V]): Option[Array[Cache.Entry[KT, V]]] = {
    runClient[V, Array[Cache.Entry[KT, V]]] { cache =>
      sqlQueryClient(cache, query, args.map(toJavaValueNull): _*).getOrElse(Array())
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

  private[this] def clientConnectHandler(adr: String, retry: Int = 0): Option[IgniteClient] = {
    import scala.concurrent.duration._
    val res = Try(Await.result(Future {
      Ignition.startClient(new ClientConfiguration().setTimeout(1000).setAddresses(s"$adr:10800"))
    }, 20.seconds))
    val client = res match {
      case scala.util.Success(client) => {
        log.info(s"connected to: $adr")
        Some(client)
      }
      case scala.util.Failure(exp) => {
        log.error(s"client could not connect ${exp} retry: $retry")
        None
      }
    }
    if (client.isEmpty && retry < 10) {
      Thread.sleep(1000L * (retry + 1))
      clientConnectHandler(adr, retry + 1)
    } else
      client
  }

  private[this] def runClient[V, R](cacheFunc: ClientCache[KT, V] => R): Option[R] = {
    val igniteClient = clientConnectHandler(server)

    if (igniteClient.isEmpty)
      throw new Exception("could not connect to Ignite giving up")

    AutoClose
      .autoClose(igniteClient.get) { ign =>
        val cache = ign.cache[KT, V](cacheName)
        cacheFunc(cache)
      }
      .toOption
  }

}
