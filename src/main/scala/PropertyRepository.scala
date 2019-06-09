import scala.async.Async.{async, await}
import io.getquill._
import org.slf4j.LoggerFactory
import QuillUtils._
import scala.concurrent.{ExecutionContext, Future}

class PropertyRepository(implicit cache: PropertyIgniteCache,
                         sqlCtx: SqlMirrorContext[MirrorSqlDialect, Literal],
                         ec: ExecutionContext) {

  lazy val logger = LoggerFactory.getLogger(classOf[PropertyRepository])

  def findByLocation(req: FindByLocation): Future[List[Property]] = {
    import sqlCtx._

    async {
      val q = quote {
        query[Property]
          .filter(p => lift(req).state.forall(_ == p.state))
          .take(100) //todo replace with paging
      }
      val sql = sqlCtx.run(q).string.fieldsToStar()
      val args = List(
        req.state,
        req.state.map(_.toLowerCase)
      )
      await { cache.query[Property](sql, args: _*).map(_.map(_.getValue).toList) }
    }
  }
}
