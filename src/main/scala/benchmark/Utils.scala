package benchmark

import benchmark.BenchmarkQueries.Strategies.{Async, Strategy}
import benchmark.BenchmarkQueries.{Query, Strategies, all, q1}
import benchmark.resolver.MainResolver
import sangria.execution.deferred.DeferredResolver
import sangria.schema.Schema

import java.io.{BufferedWriter, File, FileWriter}

object Utils {
  def resolveStrategy(args: Array[String]): (Strategy, Query) = args.toList match {
    case List(s, q) => val strategy = resolveStrategy(s); (strategy, resolveQuery(q)(strategy))
    case _ => (Async, q1(Async))
  }

  def resolveStrategy(arg: String): Strategies.Strategy = arg.toLowerCase match {
    case "async" => Strategies.Async
    case "batched" => Strategies.Batched
    case "cached" => Strategies.Cached
    case "batchedcached" => Strategies.BatchedCached
    case _ => println("Unknown strategy, using default"); Strategies.Async
  }

  def resolveQuery(q: String): Strategy => Query = s => all(s).find(_.name == q).getOrElse(q1(s))

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

  def saveToFile(text: String, queryId: String, strategyName: String, path: String = s"results/%s/log-%s.txt"): Unit = {
    val file = new File(path.format(queryId, strategyName))
    val bw = new BufferedWriter(new FileWriter(file, true))
    bw.write(text)
    bw.close()
  }
}
