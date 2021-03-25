package benchmark

import benchmark.Execution.ex
import benchmark.api.QueriesSchema
import benchmark.resolver._
import sangria.execution.deferred.DeferredResolver

object Run extends App {

  val resolvers: DeferredResolver[MainResolver] = DeferredResolver.fetchers(
    CityResolver.cachedCityResolver, CityResolver.batchedCityResolver, CityResolver.batchedCachedCityResolver,
    ContinentResolver.batchedContinentResolver, ContinentResolver.cachedContinentResolver, ContinentResolver.batchedCachedContinentResolver,
    CountryResolver.cachedCountryResolver, CountryResolver.batchedCountryResolver, CountryResolver.batchedCachedCountryResolver,
    PersonResolver.cachedPersonResolver, PersonResolver.batchedPersonResolver, PersonResolver.batchedCachedPersonResolver,
    UniversityResolver.cachedUniversityResolver, UniversityResolver.batchedCachedUniversityResolver, UniversityResolver.batchedUniversityResolver
  )

  val execution = new Execution(MainResolver.build, QueriesSchema.benchmarkQuerySchema, Some(resolvers))
  val res = execution.graphql(BenchmarkQueries.q1)
  res.onComplete(x => {
    println(x)
    Execution.stop()
  })
}
