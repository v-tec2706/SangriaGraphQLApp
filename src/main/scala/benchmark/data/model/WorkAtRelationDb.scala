package benchmark.data.model

import slick.jdbc.H2Profile.api._

import java.time.LocalDate

class WorkAtRelationDb(tag: Tag) extends Table[(Long, Long, LocalDate)](tag, "WorkAtRelation") {
  override def * = (personId, companyId, startDate)

  def companyId = column[Long]("companyId")

  def personFk = foreignKey("personFk", personId, PersonDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def personId = column[Long]("personId")

  def companyFk = foreignKey("companyFk", companyId, CompanyDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def startDate = column[LocalDate]("startDate")
}

object WorkAtRelationDb {
  val table = TableQuery[WorkAtRelationDb]
}
