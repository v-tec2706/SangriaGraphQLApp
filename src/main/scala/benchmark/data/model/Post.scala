package benchmark.data.model

import benchmark.data.model.Post.PostRecord
import database.PostgresProfile$.api._

class Post(tag: Tag) extends slick.jdbc.H2Profile.api.Table[PostRecord](tag, "Post") {
  override def * = (forumId, language, imageFile)

  def language = column[List[String]]("email")

  def imageFile = column[List[String]]("imageFile")

  def forumId = column[Long]("forumId")

  def forumFk = foreignKey("forumFk", forumId, Forum.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object Post {
  type PostRecord = (Long, List[String], List[String])
  val table = TableQuery[Post]
}