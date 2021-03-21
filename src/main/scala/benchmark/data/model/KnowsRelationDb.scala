package benchmark.data.model

import slick.jdbc.H2Profile.api._

class KnowsRelationDb(tag: Tag) extends Table[(Long, Long)](tag, "KnowsRelation") {
  override def * = (personId, friendId)

  def friendId = column[Long]("friendId")

  def personFk = foreignKey("personFk", personId, PersonDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def personId = column[Long]("personId")

  def friendFk = foreignKey("friendFk", personId, PersonDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object KnowsRelationDb {
  val table = TableQuery[KnowsRelationDb]
}

