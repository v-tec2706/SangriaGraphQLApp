package benchmark.data.model

import slick.jdbc.H2Profile.api._

class MessageTagRelation(tag: Tag) extends Table[(Long, Long)](tag, "MessageTagRelation") {
  override def * = (messageId, tagId)

  def messageFk = foreignKey("messageFk", messageId, Message.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def messageId = column[Long]("messageId")

  def tagFk = foreignKey("tagFk", tagId, TagClass.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def tagId = column[Long]("tagId")
}

object MessageTagRelation {
  val table = TableQuery[MessageTagRelation]
}
