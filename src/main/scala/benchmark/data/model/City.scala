package benchmark.data.model

import benchmark.data.model.City.CityRecord
import slick.jdbc.H2Profile.api._

class City(tag: Tag) extends Table[CityRecord](tag, "City") {
  override def * = (id, countryId, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")

  def countryId = column[Long]("countryId")

  def countryFk = foreignKey("countryFk", countryId, Country.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object City {
  type CityRecord = (Long, Long, String, String)
  val table = TableQuery[City]
}
