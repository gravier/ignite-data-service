import enumeratum.EnumEntry.Lowercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import Property.geoIdx
import org.apache.ignite.cache.query.annotations.QuerySqlField
import org.apache.ignite.cache.query.annotations.QuerySqlField.Group

import scala.annotation.meta.field
case class FindByLocation(state: Option[String] = None,
                          place: Option[String] = None,
                          propertyType: Option[String] = None,
//                          fromPriceUsd: Option[Double] = None,
//                          fromDate: Option[LocalDateTime] = None,
                          sorting: Option[List[Sorting]] = None)

case class Sorting(field: SortField, direction: SortDirection)

sealed trait SortDirection extends EnumEntry
object SortDirection extends Enum[SortDirection] with CirceEnum[SortDirection] with Lowercase {
  case object asc  extends SortDirection
  case object desc extends SortDirection
  val values = findValues
}

sealed trait SortField extends EnumEntry
object SortField extends Enum[SortField] with CirceEnum[SortField] with Lowercase {
  case object createdOn extends SortField
  val values = findValues
}

case class Property(
    @(QuerySqlField @field)(index = true) id: String,
    @(QuerySqlField @field)(orderedGroups = Array(new Group(name = geoIdx, order = 9, descending = true)))
    createdOn: Long,
    @(QuerySqlField @field)(index = true) propertyType: String,
    @(QuerySqlField @field)(orderedGroups = Array(new Group(name = geoIdx, order = 2)))
    place: String,
    @(QuerySqlField @field)(orderedGroups = Array(new Group(name = geoIdx, order = 1)))
    state: String,
    @(QuerySqlField @field)(index = true) lat: Double,
    @(QuerySqlField @field)(index = true) lon: Double,
    @(QuerySqlField @field)(index = true) price: Double,
    @(QuerySqlField @field)(index = true) currency: String,
    @(QuerySqlField @field)(index = true) priceUsd: Double,
    @(QuerySqlField @field)(index = true) surficeTotalSqm: Int,
    @(QuerySqlField @field)(index = true) surficeCoveredSqm: Int,
    @(QuerySqlField @field)(index = true) priceUsdPerSqm: Double,
    @(QuerySqlField @field)(index = true) pricePerSqm: Double,
    @(QuerySqlField @field)(index = true) floor: Int,
    @(QuerySqlField @field)(index = true) rooms: Int,
    @(QuerySqlField @field)(index = true) expenses: Int,
    url: String,
    @(QuerySqlField @field)(index = true) title: String,
    imageUrl: String)

object Property {
  final val geoIdx = "createdOn_state_place_idx"
}
