package benchmark.api.batchcache

import benchmark.api.Arguments.Id
import benchmark.resolver._
import sangria.execution.deferred.DeferredResolver
import sangria.schema.{Field, ObjectType, Schema, fields}

object QueriesSchema {
  lazy val batchedCachedResolvers: DeferredResolver[MainResolver] = DeferredResolver.fetchers(
    CityResolver.batchedCachedCityResolver,
    MessageResolver.batchedCachedMessageResolver,
    ContinentResolver.batchedCachedContinentResolver,
    CountryResolver.batchedCachedCountryResolver,
    PersonResolver.batchedCachedPersonResolver,
    UniversityResolver.batchedCachedUniversityResolver
  )
  val query: ObjectType[MainResolver, Unit] = ObjectType(
    "Query",
    fields[MainResolver, Unit](
      Field(
        "personBatchedCached",
        BatchedCachedEntities.Person,
        arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedCachedPersonResolver.defer(ctx.arg(Id).toLong)
      ),
      Field(
        "personWithArgsBatchedCached",
        BatchedCachedEntities.PersonWithArgs,
        arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedCachedPersonResolver.defer(ctx.arg(Id).toLong)
      )
    )
  )
  val batchedCachedSchema: Schema[MainResolver, Unit] = Schema(query)
}
