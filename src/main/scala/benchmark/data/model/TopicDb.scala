package benchmark.data.model

import benchmark.data.model.TopicDb.TopicRecord
import slick.jdbc.H2Profile.api._

class TopicDb(tag: Tag) extends Table[TopicRecord](tag, "Topic") {
  override def * = (id, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")
}

object TopicDb {
  type TopicRecord = (Long, String, String)
  val table = TableQuery[TopicDb]
}