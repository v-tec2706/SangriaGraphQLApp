package benchmark.data.model

import slick.jdbc.H2Profile.api._

class LikesRelation(tag: Tag) extends Table[(Long, Long)](tag, "LikesRelation") {
  override def * = (personId, messageId)

  def personId = column[Long]("personId")

  def messageId = column[Long]("messageId")

  def personFk = foreignKey("personFk", personId, Person.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def messageFk = foreignKey("messageFk", messageId, Message.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object LikesRelation {
  val table = TableQuery[LikesRelation]
}
