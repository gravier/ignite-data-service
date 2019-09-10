import java.net.URL

import kantan.csv._
import kantan.csv.ops._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
object PropertyIgniteCacheLoader {
  lazy val logger = LoggerFactory.getLogger(classOf[PropertyIgniteCache])

  def loadPropertiesFromCsv(igniteCache: PropertyIgniteCache, fullUrl: String)(implicit ec: ExecutionContext) = {

    Future {
      logger.info(s"start loading cache from: $fullUrl")
      val url      = new URL(fullUrl)
      val iterator = url.asCsvReader[List[String]](rfc.withHeader)
      val res = for { row <- iterator } yield
        row.getOrElse(List()) match {
          case (id ::
                createdOn ::
                propertyType ::
                place ::
                state ::
                lat ::
                lon ::
                price ::
                currency ::
                priceUsd ::
                surficeTotalSqm ::
                surficeCoveredSqm ::
                priceUsdPerSqm ::
                pricePerSqm ::
                floor ::
                rooms ::
                expenses ::
                url ::
                title ::
                imageUrl :: _) =>
            Some(
              Property(
                id,
                Try(org.joda.time.LocalDate.parse(createdOn).toDate.toInstant.toEpochMilli).getOrElse(0),
                propertyType.toLowerCase,
                place.toLowerCase,
                state.toLowerCase,
                Try(lat.toDouble).getOrElse(0.0),
                Try(lon.toDouble).getOrElse(0.0),
                Try(price.toDouble).getOrElse(0.0),
                currency.toLowerCase,
                Try(priceUsd.toDouble).getOrElse(0.0),
                Try(surficeTotalSqm.toInt).getOrElse(0),
                Try(surficeCoveredSqm.toInt).getOrElse(0),
                Try(priceUsdPerSqm.toDouble).getOrElse(0.0),
                Try(pricePerSqm.toDouble).getOrElse(0.0),
                Try(floor.toInt).getOrElse(0),
                Try(rooms.toInt).getOrElse(0),
                Try(expenses.toInt).getOrElse(0),
                url,
                title,
                imageUrl
              ))
          case list =>
            logger.warn(s"could not parse $list")
            None
        }

      // TODO EXERCISE 0 - load to cache
      logger.info(s"cache load finished for: $fullUrl")
    }
  }
}
