package benchmark.data.model

import benchmark.data.model.CountryDb.CountryRecord
import slick.jdbc.H2Profile.api._

class CountryDb(tag: Tag) extends Table[CountryRecord](tag, "Country") {
  override def * = (id, continentId, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")

  def continentId = column[Long]("continentId")

  def continentFk = foreignKey("continentFk", continentId, ContinentDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object CountryDb {
  type CountryRecord = (Long, Long, String, String)
  val table = TableQuery[CountryDb]
}

