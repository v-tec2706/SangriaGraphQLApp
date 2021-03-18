package benchmark.data.model

import slick.jdbc.H2Profile.api._

class KnowsRelation(tag: Tag) extends Table[(Long, Long)](tag, "KnowsRelation") {
  override def * = (personId, friendId)

  def friendId = column[Long]("friendId")

  def personFk = foreignKey("personFk", personId, Person.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def personId = column[Long]("personId")

  def friendFk = foreignKey("friendFk", personId, Person.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object KnowsRelation {
  val table = TableQuery[KnowsRelation]
}

