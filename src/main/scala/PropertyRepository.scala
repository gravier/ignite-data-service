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
          .filter(p => lift(req).propertyType.forall(_ == p.propertyType))
          .take(100) //todo replace with paging
      }

      val qSorted = req.sorting match {
        case Some(Sorting(SortField.createdOn, SortDirection.asc) :: _)  => quote { q.sortBy(_.createdOn)(Ord.asc) }
        case Some(Sorting(SortField.createdOn, SortDirection.desc) :: _) => quote { q.sortBy(_.createdOn)(Ord.desc) }
        case _                                                           => q
      }

      val args = List(
        req.state.map(_.toLowerCase),
        req.state.map(_.toLowerCase),
        req.propertyType.map(_.toLowerCase),
        req.propertyType.map(_.toLowerCase)
      )
      val sql       = sqlCtx.run(qSorted).string.fieldsToStar().removeEmptyOrFilters(args)
      val dedupArgs = removeDuplicateVals(args)
      logger.debug(s"querying: $sql")
      logger.debug(s"with args: $dedupArgs")
      await { cache.query[Property](sql, dedupArgs: _*).map(_.map(_.getValue).toList) }
    }
  }
}
