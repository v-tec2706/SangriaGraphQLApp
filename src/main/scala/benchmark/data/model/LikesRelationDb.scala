package benchmark.data.model

import slick.jdbc.H2Profile.api._

class LikesRelationDb(tag: Tag) extends Table[(Long, Long)](tag, "LikesRelation") {
  override def * = (personId, messageId)

  def personId = column[Long]("personId")

  def messageId = column[Long]("messageId")

  def personFk = foreignKey("personFk", personId, PersonDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def messageFk = foreignKey("messageFk", messageId, MessageDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object LikesRelationDb {
  val table = TableQuery[LikesRelationDb]
}
