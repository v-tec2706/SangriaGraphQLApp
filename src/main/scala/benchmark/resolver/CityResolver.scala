package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.City
import benchmark.repository.{CityRepository, Repository}
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class CityResolver(cityRepository: CityRepository) extends Resolver {
  def getCity(id: Long): Future[City] = Repository.database.run(cityRepository.getCity(id).map(_.head))

  def getCities(id: Seq[Long]): Future[Seq[City]] = Repository.database.run(cityRepository.getCities(id.toList))
}

object CityResolver {
  implicit val hasId: HasId[City, Long] = HasId[City, Long](_.id)
  val batchedCityResolver: Fetcher[MainResolver, City, City, Long] = Fetcher((ctx: MainResolver, ids: Seq[Long]) => ctx.cityResolver.getCities(ids))
  val cachedCityResolver: Fetcher[MainResolver, City, City, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[City, NoStream, Effect.All]] = ids.map(id => ctx.cityResolver.cityRepository.getCity(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[City], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    })
  val batchedCachedCityResolver: Fetcher[MainResolver, City, City, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.cityResolver.getCities(ids)
  )
}
