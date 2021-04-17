package benchmark.data.model

import slick.jdbc.H2Profile.api._

class MessageTagRelationDb(tag: Tag) extends Table[(Long, Long)](tag, "MessageTagRelation") {
  override def * = (messageId, tagId)

  def messageFk =
    foreignKey("messageFk", messageId, MessageDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def messageId = column[Long]("messageId")

  def tagFk = foreignKey("tagFk", tagId, TagClassDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def tagId = column[Long]("tagId")
}

object MessageTagRelationDb {
  val table = TableQuery[MessageTagRelationDb]
}
