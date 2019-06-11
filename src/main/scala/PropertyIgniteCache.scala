import akka.actor.Scheduler
import com.jasonmar.ignite.config.IgniteClientConfig
import com.jasonmar.ignite.{CacheBuilder, init}
import org.apache.ignite.Ignite
import org.slf4j.LoggerFactory

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future}
class PropertyIgniteCache(val cacheName: String)(implicit val ex: ExecutionContext,
                                                 val s: Scheduler,
                                                 val ignite: Ignite)
    extends IgniteMemCache[String] {

  val logger = LoggerFactory.getLogger(classOf[PropertyIgniteCache])
}

object PropertyIgniteCache {
  def cacheBuilder(cacheName: String): CacheBuilder[String, Property] = {
    CacheBuilder.builderOf(cacheName, classOf[String], classOf[Property])
  }

  def apply[KT](cacheName: String = "propertyCache")(implicit ex: ExecutionContext,
                                                     s: Scheduler,
                                                     cfg: IgniteClientConfig): Future[PropertyIgniteCache] = {
    async {
      val cacheBuilders           = Some(Seq(cacheBuilder(cacheName)))
      implicit val ignite: Ignite = init(Some(Seq(cfg)), cacheBuilders, None, activate = true)
      new PropertyIgniteCache(cacheName)
    }
  }
}
