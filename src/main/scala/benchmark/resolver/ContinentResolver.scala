package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Continent
import benchmark.repository.{ContinentRepository, Repository}
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class ContinentResolver(continentRepository: ContinentRepository) extends Resolver {
  def getContinent(id: Long): Future[Continent] = Repository.database.run(continentRepository.getContinent(id).map(_.head))

  def getContinents(id: Seq[Long]): Future[Seq[Continent]] = Repository.database.run(continentRepository.getContinents(id.toList))
}

object ContinentResolver {
  implicit val hasId: HasId[Continent, Long] = HasId[Continent, Long](_.id)
  val batchedContinentResolver: Fetcher[MainResolver, Continent, Continent, Long] = Fetcher((ctx: MainResolver, ids: Seq[Long]) => ctx.continentResolver.getContinents(ids))
  val cachedContinentResolver: Fetcher[MainResolver, Continent, Continent, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[Continent, NoStream, Effect.All]] = ids.map(id => ctx.continentResolver.continentRepository.getContinent(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[Continent], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    })
  val batchedCachedContinentResolver: Fetcher[MainResolver, Continent, Continent, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.continentResolver.getContinents(ids)
  )
}