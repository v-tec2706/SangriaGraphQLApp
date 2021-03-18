package benchmark.data.model

import slick.jdbc.H2Profile.api._

class WorkAtRelation(tag: Tag) extends Table[(Long, Long)](tag, "WorkAtRelation") {
  override def * = (personId, companyId)

  def companyId = column[Long]("companyId")

  def personFk = foreignKey("personFk", personId, Person.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def personId = column[Long]("personId")

  def companyFk = foreignKey("companyFk", companyId, Company.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}

object WorkAtRelation {
  val table = TableQuery[WorkAtRelation]
}
