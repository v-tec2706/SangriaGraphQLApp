package benchmark.api.batch

import benchmark.api.Arguments.Id
import benchmark.resolver._
import sangria.execution.deferred.DeferredResolver
import sangria.schema.{Field, ObjectType, Schema, fields}

object QueriesSchema {
  lazy val batchedResolvers = DeferredResolver.fetchers(
    CityResolver.batchedCityResolver,
    MessageResolver.batchedMessageResolver,
    ContinentResolver.batchedContinentResolver,
    CountryResolver.batchedCountryResolver,
    PersonResolver.batchedPersonResolver,
    UniversityResolver.batchedUniversityResolver
  )
  val query: ObjectType[MainResolver, Unit] = ObjectType(
    "Query",
    fields[MainResolver, Unit](
      Field(
        "personBatched",
        BatchedEntities.Person,
        arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.arg(Id).toLong)
      ),
      Field(
        "personWithArgsBatched",
        BatchedEntities.PersonWithArgs,
        arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.arg(Id).toLong)
      )
    )
  )
  val batchedSchema: Schema[MainResolver, Unit] = Schema(query)
}
