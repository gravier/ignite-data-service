import java.net.URL

import akka.actor.Scheduler
import akka.event.NoLogging
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.jasonmar.ignite.config.IgniteClientConfig
import org.scalatest._
import io.circe.generic.auto._
import io.getquill.{Literal, MirrorSqlDialect, SqlMirrorContext}
import org.apache.ignite.cache.query.SqlFieldsQuery
import org.joda.time.DateTime
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

class IgniteServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with PropertyService {
  override def testConfigSource     = "akka.loglevel = WARNING"
  override def config               = testConfig
  override val logger               = NoLogging
  implicit val scheduler: Scheduler = null // not needed in tests
  override implicit def executor    = system.dispatcher

  val minDate = new DateTime(2000, 1, 1, 0, 0, 0, 0).getMillis
  val properties = List[Property](
    Property("1",
             minDate,
             "flat",
             "rio",
             "rio",
             56.2135,
             46.222,
             3000000,
             "brl",
             3000000,
             773724,
             200,
             250,
             3868.625,
             15000,
             5,
             5,
             40000,
             "",
             "favela",
             "")
  )

  implicit val igniteCfg: IgniteClientConfig = IgniteClientConfig(peerClassLoading = true)
  implicit val cache: PropertyIgniteCache =
    ScalaFutures.whenReady(PropertyIgniteCache("testPropertyCache"), Timeout(15.seconds)) { ch =>
      Await.result(Future {
        ch.mkCache[String, Property]().clear()
      }, 15.seconds) // not to conflict between tests
      ScalaFutures.whenReady(
        Future {
          properties.foreach(p => ch.mkCache[String, Property]().put(p.id, p))
        },
        Timeout(15.second)
      ) { _ =>
        ch
      }
    }

  implicit val sqlCtx: SqlMirrorContext[MirrorSqlDialect, Literal] = new SqlMirrorContext(MirrorSqlDialect, Literal)
  implicit val propertyRepository                                  = new PropertyRepository()

//  it should "return all results" in {
//    Post(s"/property", FindByLocation()) ~> routes ~> check {
//      status shouldBe OK
//      contentType shouldBe `application/json`
//      val resp = responseAs[List[Property]]
//      resp shouldBe properties
//    }
//  }
//
//  it should "filter by state" in {
//    Post(s"/property", FindByLocation(state = Some("rio"))) ~> routes ~> check {
//      status shouldBe OK
//      contentType shouldBe `application/json`
//      val resp = responseAs[List[Property]]
//      resp shouldBe properties.filter(_.state == "rio")
//    }
//
//    Post(s"/property", FindByLocation(state = Some("rio1"))) ~> routes ~> check {
//      status shouldBe OK
//      contentType shouldBe `application/json`
//      val resp = responseAs[List[Property]]
//      resp shouldBe List()
//    }
//  }

  it should "be possible to load data" in {
    import kantan.csv._
    import kantan.csv.ops._
    import kantan.csv.generic._

    val url      = new URL("https://storage.googleapis.com/stacktome-temp/property-br-sample.csv")
    val iterator = url.asCsvReader[Property](rfc.withHeader)
  }

}
