package benchmark

import benchmark.BenchmarkQueries.Strategies
import benchmark.BenchmarkQueries.Strategies.Strategy
import benchmark.api.async.QueriesSchema
import benchmark.resolver.MainResolver

object ExecutorProvider {
  def provide(strategy: Strategy): Execution[MainResolver] =
    List(new Execution(MainResolver.build, QueriesSchema.asyncSchema, None, Strategies.Async),
      new Execution(MainResolver.build, api.batch.QueriesSchema.batchedSchema, Some(api.batch.QueriesSchema.batchedResolvers), Strategies.Batched),
      new Execution(MainResolver.build, api.cache.QueriesSchema.cachedSchema, Some(api.cache.QueriesSchema.cachedResolvers), Strategies.Cached),
      new Execution(MainResolver.build, api.batchcache.QueriesSchema.batchedCachedSchema, Some(api.batchcache.QueriesSchema.batchedCachedResolvers), Strategies.BatchedCached)
    ).find(_.strategy(strategy)).getOrElse(new Execution(MainResolver.build, QueriesSchema.asyncSchema, None, Strategies.Async))
}
