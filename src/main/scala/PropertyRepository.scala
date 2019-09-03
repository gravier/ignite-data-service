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
      val args = List(
        req.state.map(_.toLowerCase),
        req.state.map(_.toLowerCase)
      )
      val sql       = sqlCtx.run(q).string.fieldsToStar().removeEmptyOrFilters(args)
      val dedupArgs = removeDuplicateVals(args)
      logger.debug(s"querying: $sql")
      logger.debug(s"with args: $dedupArgs")
      await { cache.query[Property](sql, dedupArgs: _*).map(_.map(_.getValue).toList) }
    }
  }
}
