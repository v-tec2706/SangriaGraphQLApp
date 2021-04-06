package benchmark.api

import benchmark.Execution._
import benchmark.api.CustomTypesSchema._
import benchmark.entities._
import benchmark.resolver.MessageResolver.batchedMessageResolver
import benchmark.resolver.PersonResolver.batchedPersonResolver
import benchmark.resolver._
import sangria.schema._

object BatchedEntities {
  lazy val Person: ObjectType[MainResolver, Person] = ObjectType(
    "Person",
    () => fields[MainResolver, Person](
      Field("id", LongType, resolve = _.value.id),
      Field("firstName", StringType, resolve = _.value.firstName),
      Field("lastName", StringType, resolve = _.value.lastName),
      Field("gender", StringType, resolve = _.value.gender),
      Field("birthday", GQLDate, resolve = _.value.birthday),
      Field("browserUsed", StringType, resolve = _.value.browserUsed),
      Field("creationDate", GQLDate, resolve = _.value.creationDate),
      Field("email", ListType(StringType), resolve = _.value.email),
      Field("speaks", ListType(StringType), resolve = _.value.speaks),
      Field("locationIP", StringType, resolve = _.value.locationIP),
      Field("messages", ListType(Message),
        resolve = ctx => ctx.ctx.messagesResolver.getBySender(ctx.value.id).map(batchedMessageResolver.deferSeq)),
      Field("knows", ListType(Person),
        resolve = ctx => ctx.ctx.personResolver.knows(ctx.value.id).map(batchedPersonResolver.deferSeq)),
      Field("city", City, resolve = ctx => CityResolver.batchedCityResolver.defer(ctx.value.cityId)),
      Field("university", University, resolve = ctx => ctx.ctx.universityResolver.byStudent(ctx.value.id).map(UniversityResolver.batchedUniversityResolver.defer)),
    )
  )

  lazy val City: ObjectType[MainResolver, City] = ObjectType(
    "City",
    () => fields[MainResolver, City](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("country", Country, resolve = ctx => CountryResolver.batchedCountryResolver.defer(ctx.value.countryId))
    )
  )

  lazy val Country: ObjectType[MainResolver, Country] = ObjectType(
    "Country",
    () => fields[MainResolver, Country](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("continent", CommonEntities.Continent, resolve = ctx => ContinentResolver.batchedContinentResolver.defer(ctx.value.continentId))
    )
  )

  lazy val University: ObjectType[MainResolver, University] = ObjectType(
    "University",
    () => fields[MainResolver, University](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("city", City, resolve = ctx => CityResolver.batchedCityResolver.defer(ctx.value.cityId))
    )
  )

  lazy val Message: ObjectType[MainResolver, Message] = ObjectType(
    "Message",
    () => fields[MainResolver, Message](
      Field("id", LongType, resolve = _.value.id),
      Field("country", Country, resolve = ctx => CountryResolver.batchedCountryResolver.defer(ctx.value.countryId)),
      Field("person", Person, resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.value.personId)),
      Field("content", StringType, resolve = _.value.content),
      Field("length", IntType, resolve = _.value.length),
      Field("browserUsed", StringType, resolve = _.value.browserUsed),
      Field("creationDate", GQLDate, resolve = _.value.creationDate),
      Field("locationIP", StringType, resolve = _.value.locationIP),
    )
  )
}
