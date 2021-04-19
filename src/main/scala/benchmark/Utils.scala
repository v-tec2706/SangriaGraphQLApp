package benchmark

import benchmark.BenchmarkQueries.Strategies
import benchmark.resolver.MainResolver
import sangria.execution.deferred.DeferredResolver
import sangria.schema.Schema

object Utils {
  def resolveStrategy(args: Array[String]): Option[Strategies.Strategy] = args.toList match {
    case List(s) => Some(resolveStrategy(s))
    case _ => None
  }

  def resolveStrategy(arg: String): Strategies.Strategy = arg.toLowerCase match {
    case "async" => Strategies.Async
    case "batched" => Strategies.Batched
    case "cached" => Strategies.Cached
    case "batchedcached" => Strategies.BatchedCached
    case _ => println("Unknown strategy, using default"); Strategies.Async
  }

  def resolveSchema(strategy: Strategies.Strategy): Schema[MainResolver, Unit] = strategy match {
    case Strategies.Async => api.async.QueriesSchema.asyncSchema
    case Strategies.Batched => api.batch.QueriesSchema.batchedSchema
    case Strategies.Cached => api.cache.QueriesSchema.cachedSchema
    case Strategies.BatchedCached => api.batchcache.QueriesSchema.batchedCachedSchema
  }

  def resolveExecutor(strategy: Strategies.Strategy): Option[DeferredResolver[MainResolver]] = strategy match {
    case Strategies.Async => None
    case Strategies.Batched => Some(api.batch.QueriesSchema.batchedResolvers)
    case Strategies.Cached => Some(api.cache.QueriesSchema.cachedResolvers)
    case Strategies.BatchedCached => Some(api.batchcache.QueriesSchema.batchedCachedResolvers)
  }
}
