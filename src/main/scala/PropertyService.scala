import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.jasonmar.ignite.config.IgniteClientConfig
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.{BaseCirceSupport, FailFastUnmarshaller}
import io.circe.generic.auto._
import io.getquill.{Literal, MirrorSqlDialect, SqlMirrorContext}
import PropertyIgniteCacheLoader._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.Failure
import SortDirection._

trait PropertyService extends BaseCirceSupport with FailFastUnmarshaller {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  implicit val propertyRepository: PropertyRepository

  def config: Config
  val logger: LoggingAdapter

  lazy val apiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("services.ip-api.host"), config.getInt("services.ip-api.port"))

  def ipApiRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(apiConnectionFlow).runWith(Sink.head)

  val routes = {
    logRequestResult("ignite-data-service") {
      pathPrefix("property") {
        (post & entity(as[FindByLocation])) { req =>
          complete {
            propertyRepository.findByLocation(req)
          }
        }
      }
    }
  }
}

object PropertyServiceApp extends App with PropertyService {
  override implicit val system       = ActorSystem()
  override implicit val executor     = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  implicit val scheduler = system.scheduler

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  implicit val igniteCfg: IgniteClientConfig =
    IgniteClientConfig(
      peerClassLoading = true
    )

  implicit val sqlCtx: SqlMirrorContext[MirrorSqlDialect, Literal] = new SqlMirrorContext(MirrorSqlDialect, Literal)
  implicit val cache: PropertyIgniteCache = Await.result(
    PropertyIgniteCache(),
    10.seconds
  )

  loadPropertiesFromCsv(cache,
                        //"https://storage.googleapis.com/stacktome-temp/property-br-sample.csv")
                        "file:////home/evaldas/Downloads/property-br-sample.csv")
//    "file:////home/evaldas/Downloads/property-br.csv"
//  )
  override implicit val propertyRepository = new PropertyRepository()

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
