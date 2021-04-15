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
    "Query", fields[MainResolver, Unit](
      Field("person", BatchedEntities.Person, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.arg(Id))
      ),
      Field("personWithArgs", BatchedEntities.PersonWithArgs, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.arg(Id))
      )
    )
  )
  val batchedSchema: Schema[MainResolver, Unit] = Schema(query)
}
