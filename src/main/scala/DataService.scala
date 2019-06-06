import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.{BaseCirceSupport, FailFastUnmarshaller}
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContextExecutor, Future}

case class FindByLocation(query: String,
                          state: Option[String] = None,
                          lat: Option[Double] = None,
                          lon: Option[Double] = None)

trait DataService extends BaseCirceSupport with FailFastUnmarshaller {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  lazy val apiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("services.ip-api.host"), config.getInt("services.ip-api.port"))

  def ipApiRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(apiConnectionFlow).runWith(Sink.head)

  val routes = {
    logRequestResult("ignite-data-service") {
      pathPrefix("realtor") {
        (post & entity(as[FindByLocation])) { req =>
          complete {
            BadRequest -> "now working"
          }
        }
      }
    }
  }
}

object DataServiceApp extends App with DataService {
  override implicit val system       = ActorSystem()
  override implicit val executor     = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
