package benchmark

import benchmark.BenchmarkQueries.Strategies
import benchmark.BenchmarkQueries.Strategies.Strategy
import benchmark.api.async.QueriesSchema
import benchmark.resolver.MainResolver

object ExecutorProvider {
  def provide(strategy: Strategy): Execution =
    List(
      StandardExecutor(MainResolver.build, QueriesSchema.asyncSchema, None, Strategies.Async),
      StandardExecutor(
        MainResolver.build,
        api.batch.QueriesSchema.batchedSchema,
        Some(api.batch.QueriesSchema.batchedResolvers),
        Strategies.Batched
      ),
      StandardExecutor(
        MainResolver.build,
        api.cache.QueriesSchema.cachedSchema,
        Some(api.cache.QueriesSchema.cachedResolvers),
        Strategies.Cached
      ),
      StandardExecutor(
        MainResolver.build,
        api.batchcache.QueriesSchema.batchedCachedSchema,
        Some(api.batchcache.QueriesSchema.batchedCachedResolvers),
        Strategies.BatchedCached
      )
    ).find(_.strategy(strategy)).getOrElse(StandardExecutor(MainResolver.build, QueriesSchema.asyncSchema, None, Strategies.Async))
}
