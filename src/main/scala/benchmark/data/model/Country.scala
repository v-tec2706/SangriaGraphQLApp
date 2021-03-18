package benchmark.data.model

import benchmark.data.model.Country.CountryRecord
import slick.jdbc.H2Profile.api._

class Country(tag: Tag) extends Table[CountryRecord](tag, "Country") {
  override def * = (id, continentId, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")

  def continentId = column[Long]("continentId")

  def continentFk = foreignKey("continentFk", continentId, Continent.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object Country {
  type CountryRecord = (Long, Long, String, String)
  val table = TableQuery[Country]
}

