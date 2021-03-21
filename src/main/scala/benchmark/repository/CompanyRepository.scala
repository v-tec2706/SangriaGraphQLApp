package benchmark.repository

import benchmark.data.model.CompanyDb
import benchmark.data.model.CompanyDb.CompanyRecord
import benchmark.entities.Company
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class CompanyRepository() extends Repository[CompanyRecord, CompanyDb, Company] {
  override def table: H2Profile.api.TableQuery[CompanyDb] = CompanyDb.table

  override def entityMapping: CompanyRecord => Company = p => Company(p._1, p._2, p._3, p._4)

  def getCompany(id: Long): dbio.DBIO[Seq[Company]] = get { c: CompanyDb => c.id === id }.map(_.map(entity))

  def getCompanies(ids: List[Long]): dbio.DBIO[Seq[Company]] = get { c: CompanyDb => c.id inSet ids }.map(_.map(entity))
}
