import enumeratum.EnumEntry.Lowercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import org.apache.ignite.cache.query.annotations.QuerySqlField
import org.apache.ignite.cache.query.annotations.QuerySqlField.Group

import scala.annotation.meta.field
case class FindByLocation(query: Option[String] = None,
                          state: Option[String] = None,
                          propertyType: Option[String] = None,
                          lat: Option[Double] = None,
                          lon: Option[Double] = None,
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
//@(QuerySqlField @field)(index = true)
//(orderedGroups = Array(new Group(name = "geoIdx", order = 9, descending = true)))

//TODO - EXERCISE 3 - adding date group indexes for performance
case class Property(@(QuerySqlField @field)(index = true) id: String,
                    @(QuerySqlField @field) createdOn: Long,
                    @(QuerySqlField @field) propertyType: String,
                    @(QuerySqlField @field) place: String,
                    @(QuerySqlField @field) state: String,
                    @(QuerySqlField @field) lat: Double,
                    @(QuerySqlField @field) lon: Double,
                    @(QuerySqlField @field) price: Double,
                    @(QuerySqlField @field) currency: String,
                    @(QuerySqlField @field) priceUsd: Double,
                    @(QuerySqlField @field) surficeTotalSqm: Int,
                    @(QuerySqlField @field) surficeCoveredSqm: Int,
                    @(QuerySqlField @field) priceUsdPerSqm: Double,
                    @(QuerySqlField @field) pricePerSqm: Double,
                    @(QuerySqlField @field) floor: Int,
                    @(QuerySqlField @field) rooms: Int,
                    @(QuerySqlField @field) expenses: Int,
                    url: String,
                    @(QuerySqlField @field) title: String,
                    imageUrl: String)
