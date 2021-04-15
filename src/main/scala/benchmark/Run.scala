package benchmark

import benchmark.BenchmarkQueries.{Strategies, all}
import benchmark.Execution.{ex, stop}
import benchmark.api.async.QueriesSchema
import benchmark.resolver._
import io.circe.Json

import scala.concurrent.Future

object Run extends App {

  val executors = List(new Execution(MainResolver.build, QueriesSchema.asyncSchema, None, Strategies.Async),
    new Execution(MainResolver.build, api.batch.QueriesSchema.batchedSchema, Some(api.batch.QueriesSchema.batchedResolvers), Strategies.Batched),
    new Execution(MainResolver.build, api.cache.QueriesSchema.cachedSchema, Some(api.cache.QueriesSchema.cachedResolvers), Strategies.Cached),
    new Execution(MainResolver.build, api.batchcache.QueriesSchema.batchedCachedSchema, Some(api.batchcache.QueriesSchema.batchedCachedResolvers), Strategies.BatchedCached)
  )

  val strategyToUse = strategy.getOrElse(Strategies.Async)

  def strategy: Option[Strategies.Strategy] = args.toList match {
    case List(s) => Some(resolveStrategy(s))
    case _ => None
  }

  def resolveStrategy(arg: String): Strategies.Strategy = arg match {
    case "async" => Strategies.Async
    case "batched" => Strategies.Batched
    case "cached" => Strategies.Cached
    case "batchedCached" => Strategies.BatchedCached
    case _ => println("Unknown strategy, using default"); Strategies.Async
  }

  println(s"Using strategy: $strategyToUse")
  executors.find(_.strategy(strategyToUse)).map(resolver => {
    Future.sequence(all(strategyToUse)
      .map { case (name, q) => (name, resolver.graphql(q)) }
      .map { case (name, res) => res.map(res => processResult(name, res)) }
    ).onComplete(_ => stop())
  })


  //    execution.graphql(BenchmarkQueries.q).onComplete(x => {
  //      println(x)
  //      processResult("q2", x)
  //    })

  def processResult: (String, Json) => Unit = (name, res) => {
    println(res)
    res.hcursor.downField("extensions")
      .downField("tracing")
      .downField("duration")
      .focus.map(x => println(s"$name, $x")
    )
  }
}

