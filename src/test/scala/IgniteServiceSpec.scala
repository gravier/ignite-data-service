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
import scala.util.Try

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
             ""),
    Property("2",
             minDate + 1,
             "house",
             "puerta",
             "puerta",
             56.2135,
             46.222,
             300002200,
             "brl",
             77372004,
             2000,
             500,
             3868.625,
             15000,
             9,
             10,
             900000,
             "",
             "mansion",
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
//  implicit val cache: PropertyIgniteCache                          = null
  implicit val sqlCtx: SqlMirrorContext[MirrorSqlDialect, Literal] = new SqlMirrorContext(MirrorSqlDialect, Literal)
  implicit val propertyRepository                                  = new PropertyRepository()
  //TODO - EXERCISE 1 - implement by propertyType and writing unit test by propertyType
  //TODO - EXERCISE 2 - sorting by date

  it should "return all results" in {
    Post(s"/property", FindByLocation()) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      val resp = responseAs[List[Property]]
      resp shouldBe properties
    }
  }

// TODO  - BONUS 1 - implement paging (why usually api's don't return count)
// TODO - BONUS 2 - implement filter by room count
// TODO - BONUS 3 - implement filter by price
// TODO - BONUS 4 - implement filter by date

}
