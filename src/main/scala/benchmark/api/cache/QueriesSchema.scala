package benchmark.api.cache

import benchmark.api.Arguments.Id
import benchmark.resolver._
import sangria.execution.deferred.DeferredResolver
import sangria.schema.{Field, ObjectType, Schema, fields}

object QueriesSchema {
  lazy val cachedResolvers: DeferredResolver[MainResolver] = DeferredResolver.fetchers(
    CityResolver.cachedCityResolver,
    MessageResolver.cachedMessageResolver,
    ContinentResolver.cachedContinentResolver,
    CountryResolver.cachedCountryResolver,
    PersonResolver.cachedPersonResolver,
    UniversityResolver.cachedUniversityResolver
  )
  val query: ObjectType[MainResolver, Unit] = ObjectType(
    "Query", fields[MainResolver, Unit](
      Field("person", CachedEntities.Person, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.cachedPersonResolver.defer(ctx.arg(Id))
      ),
      Field("personWithArgs", CachedEntities.PersonWithArgs, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.cachedPersonResolver.defer(ctx.arg(Id))
      )
    )
  )
  val cachedSchema: Schema[MainResolver, Unit] = Schema(query)
}
