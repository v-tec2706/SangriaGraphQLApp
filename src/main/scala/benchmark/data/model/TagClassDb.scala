package benchmark.data.model

import benchmark.data.model.TagClassDb.TagClassRecord
import slick.jdbc.H2Profile.api._

class TagClassDb(tag: Tag) extends Table[TagClassRecord](tag, "TagClass") {
  override def * = (id, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")
}

object TagClassDb {
  type TagClassRecord = (Long, String, String)
  val table = TableQuery[TagClassDb]
}
