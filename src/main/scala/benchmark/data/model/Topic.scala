package benchmark.data.model

import benchmark.data.model.Topic.TopicRecord
import slick.jdbc.H2Profile.api._

class Topic(tag: Tag) extends Table[TopicRecord](tag, "Topic") {
  override def * = (id, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")
}

object Topic {
  type TopicRecord = (Long, String, String)
  val table = TableQuery[Topic]
}