import scala.async.Async.{async, await}
import io.getquill._
import org.slf4j.LoggerFactory
import QuillUtils._
import scala.concurrent.{ExecutionContext, Future}

class PropertyRepository(implicit cache: PropertyIgniteCache,
                         sqlCtx: SqlMirrorContext[MirrorSqlDialect, Literal],
                         ec: ExecutionContext) {

  lazy val logger = LoggerFactory.getLogger(classOf[PropertyRepository])

  def findByLocation(req: FindByLocation): Future[List[Property]] = ???
}
