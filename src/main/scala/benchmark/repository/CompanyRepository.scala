package benchmark.repository

import benchmark.data.model.CompanyDb.CompanyRecord
import benchmark.data.model.{CompanyDb, WorkAtRelationDb}
import benchmark.entities.Company
import slick.dbio
import slick.dbio.Effect
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.sql.FixedSqlStreamingAction

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

case class CompanyRepository() extends Repository[CompanyRecord, CompanyDb, Company] {
  override def table: H2Profile.api.TableQuery[CompanyDb] = CompanyDb.table

  override def entityMapping: CompanyRecord => Company = p => Company(p._1, p._2, p._3, p._4)

  def getCompany(id: Long): dbio.DBIO[Seq[Company]] = get { c: CompanyDb => c.id === id }.map(_.map(entity))

  def getCompanies(ids: List[Long]): dbio.DBIO[Seq[Company]] = get { c: CompanyDb => c.id inSet ids }.map(_.map(entity))

  def worksAt(personId: Long): FixedSqlStreamingAction[Seq[(Long, Long, LocalDate)], (Long, Long, LocalDate), Effect.Read] =
    WorkAtRelationDb.table.filter(_.personId === personId).map(x => (x.personId, x.companyId, x.startDate)).result

  def workAt(personIds: List[Long]): FixedSqlStreamingAction[Seq[(Long, Long, LocalDate)], (Long, Long, LocalDate), Effect.Read] =
    WorkAtRelationDb.table.filter(_.personId inSet personIds).map(x => (x.personId, x.companyId, x.startDate)).result
}
