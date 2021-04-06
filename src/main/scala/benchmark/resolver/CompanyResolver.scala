package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Company
import benchmark.repository.{CompanyRepository, Repository}

import java.time.LocalDate
import scala.concurrent.Future

case class CompanyResolver(companyRepository: CompanyRepository) extends Resolver {
  def getCompany(id: Long): Future[Company] = Repository.database.run(companyRepository.getCompany(id).map(_.head))

  def getCompanies(ids: Seq[Long]): Future[Seq[Company]] = Repository.database.run(companyRepository.getCompanies(ids.toList))

  def worksAt(personId: Long): Future[Seq[Long]] = Repository.database.run(companyRepository.worksAt(personId))

  def workAt(personIds: List[Long]): Future[Seq[(Long, Long, LocalDate)]] = Repository.database.run(companyRepository.workAt(personIds))
}