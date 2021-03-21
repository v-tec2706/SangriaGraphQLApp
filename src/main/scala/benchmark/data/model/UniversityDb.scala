package benchmark.data.model

import benchmark.data.model.UniversityDb.UniversityRecord
import slick.jdbc.H2Profile.api._

class UniversityDb(tag: Tag) extends Table[UniversityRecord](tag, "University") {
  override def * = (id, cityId, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")

  def cityFk = foreignKey("cityFk", cityId, CountryDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def cityId = column[Long]("cityId")
}

object UniversityDb {
  type UniversityRecord = (Long, Long, String, String)
  val table = TableQuery[UniversityDb]
}