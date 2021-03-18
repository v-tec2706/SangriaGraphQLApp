package benchmark.data.model

import benchmark.data.model.Continent.ContinentRecord
import slick.jdbc.H2Profile.api._

class Continent(tag: Tag) extends Table[ContinentRecord](tag, "Continent") {
  override def * = (id, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")
}

object Continent {
  type ContinentRecord = (Long, String, String)
  val table = TableQuery[Continent]
}
