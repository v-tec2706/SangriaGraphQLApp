package benchmark.data.model

import slick.jdbc.H2Profile.api._

class TagClass(tag: Tag) extends Table[(Long, String, String)](tag, "TagClass") {
  override def * = (id, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")
}

object TagClass {
  type TagClassRecord = (Long, String, String)
  val table = TableQuery[TagClass]
}