package benchmark.api

import benchmark.api.CustomTypesSchema._
import benchmark.entities._
import benchmark.resolver.Resolver
import sangria.schema._

object BenchmarkTypesSchema {
  lazy val Person: ObjectType[Resolver, Person] = ObjectType(
    "Person",
    () => fields[Resolver, Person](
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
      Field("messages", ListType(Message), resolve = ctx => ctx.ctx.messagesResolver.getBySender(ctx.value.id)),
      Field("knows", ListType(Person), resolve = ctx => ctx.ctx.personResolver.knows(ctx.value.id)),
      Field("city", City, resolve = ctx => ctx.ctx.cityResolver.getCity(ctx.value.cityId)),
      Field("university", University, resolve = ctx => ctx.ctx.universityResolver.byStudent(ctx.value.id)),
    )
  )

  lazy val City: ObjectType[Resolver, City] = ObjectType(
    "City",
    () => fields[Resolver, City](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("country", Country, resolve = ctx => ctx.ctx.countryResolver.getCountry(ctx.value.countryId))
    )
  )

  lazy val Country: ObjectType[Resolver, Country] = ObjectType(
    "Country",
    () => fields[Resolver, Country](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("continent", Continent, resolve = ctx => ctx.ctx.continentResolver.getContinent(ctx.value.continentId))
    )
  )

  lazy val Continent: ObjectType[Resolver, Continent] = ObjectType(
    "Continent",
    () => fields[Resolver, Continent](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
    )
  )

  lazy val University: ObjectType[Resolver, University] = ObjectType(
    "University",
    () => fields[Resolver, University](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("city", City, resolve = ctx => ctx.ctx.cityResolver.getCity(ctx.value.cityId))
    )
  )

  lazy val Message: ObjectType[Resolver, Message] = ObjectType(
    "Message",
    () => fields[Resolver, Message](
      Field("id", LongType, resolve = _.value.id),
      Field("country", Country, resolve = ctx => ctx.ctx.countryResolver.getCountry(ctx.value.countryId)),
      Field("person", Person, resolve = ctx => ctx.ctx.personResolver.getPerson(ctx.value.personId)),
      Field("content", StringType, resolve = _.value.content),
      Field("length", IntType, resolve = _.value.length),
      Field("browserUsed", StringType, resolve = _.value.browserUsed),
      Field("creationDate", GQLDate, resolve = _.value.creationDate),
      Field("locationIP", StringType, resolve = _.value.locationIP),
    )
  )
}
