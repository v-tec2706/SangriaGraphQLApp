package benchmark.data.model

import slick.jdbc.H2Profile.api._

class ForumTagRelationDb(tag: Tag) extends Table[(Long, Long)](tag, "ForumTagRelation") {
  override def * = (forumId, tagId)

  def tagId = column[Long]("tagId")

  def forumFk = foreignKey("forumFk", forumId, ForumDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def forumId = column[Long]("forumId")

  def tagFk = foreignKey("tagFk", tagId, TagClassDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object ForumTagRelationDb {
  val table = TableQuery[ForumTagRelationDb]
}
