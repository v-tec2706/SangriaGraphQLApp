package benchmark

import benchmark.BenchmarkQueries.{Strategies, all}
import benchmark.Execution.{ex, stop}
import benchmark.api.QueriesSchema
import benchmark.resolver._
import io.circe.Json
import sangria.execution.deferred.DeferredResolver

import scala.concurrent.Future

object Run extends App {

  val resolvers: DeferredResolver[MainResolver] = DeferredResolver.fetchers(
    CityResolver.cachedCityResolver, CityResolver.batchedCityResolver, CityResolver.batchedCachedCityResolver,
    MessageResolver.cachedMessageResolver, MessageResolver.batchedMessageResolver, MessageResolver.batchedCachedMessageResolver,
    ContinentResolver.batchedContinentResolver, ContinentResolver.cachedContinentResolver, ContinentResolver.batchedCachedContinentResolver,
    CountryResolver.cachedCountryResolver, CountryResolver.batchedCountryResolver, CountryResolver.batchedCachedCountryResolver,
    PersonResolver.cachedPersonResolver, PersonResolver.batchedPersonResolver, PersonResolver.batchedCachedPersonResolver,
    UniversityResolver.cachedUniversityResolver, UniversityResolver.batchedCachedUniversityResolver, UniversityResolver.batchedUniversityResolver
  )
  val execution = new Execution(MainResolver.build, QueriesSchema.benchmarkQuerySchema, Some(resolvers))

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

  val strategyToUse = strategy.getOrElse(Strategies.Async)
  println(s"Using strategy: $strategyToUse")
  Future.sequence(all(strategyToUse)
    .map { case (name, q) => (name, execution.graphql(q)) }
    .map { case (name, res) => res.map(res => processResult(name, res)) }
  ).onComplete(_ => stop())

  //    execution.graphql(BenchmarkQueries.q).onComplete(x => {
  //      println(x)
  //      processResult("q2", x)
  //    })

  def processResult: (String, Json) => Unit = (name, res) => {
    res.hcursor.downField("extensions")
      .downField("tracing")
      .downField("duration")
      .focus.map(x => println(s"$name, $x")
    )
  }
}

