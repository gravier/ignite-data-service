import scala.async.Async.{async, await}
import io.getquill._
import org.slf4j.LoggerFactory
import QuillUtils._
import scala.concurrent.{ExecutionContext, Future}

class PropertyRepository(implicit cache: PropertyIgniteCache,
                         sqlCtx: SqlMirrorContext[MirrorSqlDialect, Literal],
                         ec: ExecutionContext) {

  lazy val logger = LoggerFactory.getLogger(classOf[PropertyRepository])

  def findByLocation(req: FindByLocation): Future[List[Property]] = async {
    import sqlCtx._
    val q = quote {
      query[Property]
        .filter(rw => lift(req.state).forall(rw.state == _))
        .filter(rw => lift(req.place).forall(rw.place == _))
        .take(100)
    }
    val sql = sqlCtx.run(q).string.fieldsToStar
    val args = List(req.state.map(_.toLowerCase),
                    req.state.map(_.toLowerCase),
                    req.place.map(_.toLowerCase),
                    req.place.map(_.toLowerCase))
    logger.info(s"property query: $sql")
    await { cache.query[Property](sql, args: _*) }.map(_.getValue).toList
  }
}
