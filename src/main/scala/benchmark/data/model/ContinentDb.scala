package benchmark.data.model

import benchmark.data.model.ContinentDb.ContinentRecord
import slick.jdbc.H2Profile.api._

class ContinentDb(tag: Tag) extends Table[ContinentRecord](tag, "Continent") {
  override def * = (id, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")
}

object ContinentDb {
  type ContinentRecord = (Long, String, String)
  val table = TableQuery[ContinentDb]
}
