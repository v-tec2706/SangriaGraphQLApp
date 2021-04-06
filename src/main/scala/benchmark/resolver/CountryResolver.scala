package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Country
import benchmark.repository.{CountryRepository, Repository}
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class CountryResolver(countryRepository: CountryRepository) extends Resolver {
  def getCountry(id: Long): Future[Country] = Repository.database.run(countryRepository.getCountry(id).map(_.head))

  def getCountryByName(name: String): Future[Country] = Repository.database.run(countryRepository.getCountry(name).map(_.head))

  def getCountries(ids: Seq[Long]): Future[Seq[Country]] = Repository.database.run(countryRepository.getCountries(ids.toList))
}

object CountryResolver {
  implicit val hasId: HasId[Country, Long] = HasId[Country, Long](_.id)
  val batchedCountryResolver: Fetcher[MainResolver, Country, Country, Long] = Fetcher((ctx: MainResolver, ids: Seq[Long]) => ctx.countryResolver.getCountries(ids))
  val cachedCountryResolver: Fetcher[MainResolver, Country, Country, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[Country, NoStream, Effect.All]] = ids.map(id => ctx.countryResolver.countryRepository.getCountry(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[Country], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    })
  val batchedCachedCountryResolver: Fetcher[MainResolver, Country, Country, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.countryResolver.getCountries(ids)
  )
}