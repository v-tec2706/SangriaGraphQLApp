package benchmark.data.model

import slick.jdbc.H2Profile.api._

class HasMemberRelationDb(tag: Tag) extends Table[(Long, Long)](tag, "HasMemberRelation") {
  override def * = (forumId, personId)

  def forumFk = foreignKey("forumFk", forumId, ForumDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def forumId = column[Long]("forumId")

  def personFk = foreignKey("personFk", personId, PersonDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def personId = column[Long]("personId")
}

object HasMemberRelationDb {
  val table = TableQuery[HasMemberRelationDb]
}
