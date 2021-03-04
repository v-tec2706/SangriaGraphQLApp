package service

import model.CharacterEntity
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}

object CreatureDeferredResolver {

  implicit val hasId = HasId[CharacterEntity, Int](_.id)
  val batchedCreatures = Fetcher(
    (ctx: CreatureService, ids: Seq[Int]) => {
      ctx.getCreaturesIn(ids.toList)
    })

  val cachedBatchedCreatures = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: CreatureService, ids: Seq[Int]) => ctx.getCreaturesIn(ids.toList)
  )

  val cachedCreatures = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: CreatureService, ids: Seq[Int]) => {
      val z = ctx.getCreatures(ids.toList)
      z
    }
  )
}
