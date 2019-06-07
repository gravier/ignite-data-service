import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._
import io.circe.generic.auto._

class IgniteServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with DataService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config           = testConfig
  override val logger           = NoLogging

//  override lazy val apiConnectionFlow = Flow[HttpRequest].map { request =>
//    if (request.uri.toString().endsWith(ip1Info.query))
//      HttpResponse(status = OK, entity = marshal(ip1Info))
//    else if (request.uri.toString().endsWith(ip2Info.query))
//      HttpResponse(status = OK, entity = marshal(ip2Info))
//    else
//      HttpResponse(status = BadRequest, entity = marshal("Bad ip format"))
//  }

  it should "return results when looking by state" in {
    Post(s"/property", FindByLocation(state = Some("rio"))) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
    }
  }

}
