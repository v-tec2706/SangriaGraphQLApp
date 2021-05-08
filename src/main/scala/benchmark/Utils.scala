package benchmark

import benchmark.BenchmarkQueries.Strategies.{Async, Batched, BatchedCached, Cached, Strategy}
import benchmark.BenchmarkQueries.{Query, Strategies, all, q1}
import benchmark.resolver.MainResolver
import sangria.execution.deferred.DeferredResolver
import sangria.schema.Schema

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

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

  def resolveServicePort(strategy: Strategy): Int = strategy match {
    case Async => 8081
    case Batched => 8082
    case Cached => 8083
    case BatchedCached => 8084
  }

  def saveToFile(text: String, queryId: String, strategyName: String, directoryName: String = s"results"): Unit = {
    val fileName = s"log-$strategyName.txt"
    val dirPath = directoryName + "/" + queryId
    val directory = Files.createDirectories(Paths.get(dirPath))
    val file = new File(dirPath + "/" + fileName)
    val bw = new BufferedWriter(new FileWriter(file, true))
    bw.write(text)
    bw.close()
  }
}
