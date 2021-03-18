package benchmark.data.model

import slick.jdbc.H2Profile.api._

class ForumTagRelation(tag: Tag) extends Table[(Long, Long)](tag, "ForumTagRelation") {
  override def * = (forumId, tagId)

  def tagId = column[Long]("tagId")

  def forumFk = foreignKey("forumFk", forumId, Forum.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def forumId = column[Long]("forumId")

  def tagFk = foreignKey("tagFk", tagId, TagClass.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object ForumTagRelation {
  val table = TableQuery[ForumTagRelation]
}
