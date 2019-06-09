import org.apache.ignite.cache.query.annotations.QuerySqlField

import scala.annotation.meta.field
case class FindByLocation(query: Option[String] = None,
                          state: Option[String] = None,
                          lat: Option[Double] = None,
                          lon: Option[Double] = None)

case class Property(@(QuerySqlField @field)(index = true) id: String,
                    @(QuerySqlField @field)(index = true) createdOn: Long,
                    @(QuerySqlField @field)(index = true) price: Double,
                    @(QuerySqlField @field)(index = true) currency: String,
                    @(QuerySqlField @field)(index = true) place: String,
                    @(QuerySqlField @field)(index = true) state: String,
                    @(QuerySqlField @field)(index = true) lat: Double,
                    @(QuerySqlField @field)(index = true) lon: Double,
                    @(QuerySqlField @field)(index = true) priceUsd: Double,
                    @(QuerySqlField @field)(index = true) priceLocal: Double,
                    @(QuerySqlField @field)(index = true) surficeTotalSqm: Int,
                    @(QuerySqlField @field)(index = true) surficeCoveredSqm: Int,
                    @(QuerySqlField @field)(index = true) priceUsdPerSqm: Double,
                    @(QuerySqlField @field)(index = true) pricePerSqm: Double,
                    @(QuerySqlField @field)(index = true) floor: Int,
                    @(QuerySqlField @field)(index = true) rooms: Int,
                    @(QuerySqlField @field)(index = true) expenses: Int,
                    @(QuerySqlField @field)(index = true) url: String,
//    description: String,
                    @(QuerySqlField @field)(index = true) title: String,
                    @(QuerySqlField @field)(index = true) imageUrl: String)
