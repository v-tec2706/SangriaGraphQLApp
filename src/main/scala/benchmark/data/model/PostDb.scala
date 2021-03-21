package benchmark.data.model

import benchmark.data.model.PostDb.PostRecord
import database.PostgresProfile$.api._

class PostDb(tag: Tag) extends slick.jdbc.H2Profile.api.Table[PostRecord](tag, "Post") {
  override def * = (forumId, language, imageFile)

  def language = column[List[String]]("language")

  def imageFile = column[List[String]]("imageFile")

  def forumId = column[Long]("forumId")

  def forumFk = foreignKey("forumFk", forumId, ForumDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object PostDb {
  type PostRecord = (Long, List[String], List[String])
  val table = TableQuery[PostDb]
}