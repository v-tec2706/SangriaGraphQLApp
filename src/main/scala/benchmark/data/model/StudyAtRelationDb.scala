package benchmark.data.model

import slick.jdbc.H2Profile.api._

class StudyAtRelationDb(tag: Tag) extends Table[(Long, Long)](tag, "StudyAtRelation") {
  override def * = (personId, universityId)

  def personFk =
    foreignKey("personFk", personId, PersonDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def personId = column[Long]("personId")

  def universityFk = foreignKey("universityFk", universityId, UniversityDb.table)(
    _.id,
    onUpdate = ForeignKeyAction.Restrict,
    onDelete = ForeignKeyAction.Cascade
  )

  def universityId = column[Long]("universityId")
}

object StudyAtRelationDb {
  val table = TableQuery[StudyAtRelationDb]
}
