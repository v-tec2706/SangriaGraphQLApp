package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Company
import benchmark.repository.{CompanyRepository, Repository}
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import java.time.LocalDate
import scala.concurrent.Future

case class CompanyResolver(companyRepository: CompanyRepository) extends Resolver {
  def getCompany(id: Long): Future[Company] = Repository.database.run(companyRepository.getCompany(id).map(_.head))

  def getCompanies(ids: Seq[Long]): Future[Seq[Company]] = Repository.database.run(companyRepository.getCompanies(ids.toList))

  def worksAt(personId: Long): Future[(Long, Long, LocalDate)] = Repository.database.run(companyRepository.worksAt(personId).head)

  def workAt(personIds: List[Long]): Future[Seq[(Long, Long, LocalDate)]] = Repository.database.run(companyRepository.workAt(personIds))
}

object CompanyResolver {
  implicit val hasId: HasId[Company, Long] = HasId[Company, Long](_.id)
  val batchedCompanyResolver: Fetcher[MainResolver, Company, Company, Long] = Fetcher((ctx: MainResolver, ids: Seq[Long]) => ctx.companyResolver.getCompanies(ids.toList))
  val cachedCompanyResolver: Fetcher[MainResolver, Company, Company, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[Company, NoStream, Effect.All]] = ids.map(id => ctx.companyResolver.companyRepository.getCompany(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[Company], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    })
  val batchedCachedCompanyResolver: Fetcher[MainResolver, Company, Company, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.companyResolver.getCompanies(ids.toList)
  )
}