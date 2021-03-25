package benchmark.api

import benchmark.Execution._
import benchmark.api.CustomTypesSchema._
import benchmark.entities._
import benchmark.resolver.MainResolver
import sangria.schema._

object AsyncEntities {
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
      Field("messages", ListType(Message), resolve = ctx => for {
        messagesId <- ctx.ctx.messagesResolver.getBySender(ctx.value.id)
        messages <- ctx.ctx.messagesResolver.getMessageAsync(messagesId.toList)
      } yield messages),
      Field("knows", ListType(Person),
        resolve = ctx => for {
          friendIds <- ctx.ctx.personResolver.knows(ctx.value.id)
          friends <- ctx.ctx.personResolver.getPeopleAsync(friendIds.toList)
        } yield friends),
      Field("city", City, resolve = ctx => ctx.ctx.cityResolver.getCity(ctx.value.cityId)),
      Field("university", University, resolve = ctx => for {
        universityId <- ctx.ctx.universityResolver.byStudent(ctx.value.id)
        university <- ctx.ctx.universityResolver.getUniversity(universityId)
      } yield university),
    )
  )

  lazy val City: ObjectType[MainResolver, City] = ObjectType(
    "City",
    () => fields[MainResolver, City](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("country", Country, resolve = ctx => ctx.ctx.countryResolver.getCountry(ctx.value.countryId))
    )
  )

  lazy val Country: ObjectType[MainResolver, Country] = ObjectType(
    "Country",
    () => fields[MainResolver, Country](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("continent", CommonEntities.Continent, resolve = ctx => ctx.ctx.continentResolver.getContinent(ctx.value.continentId))
    )
  )

  lazy val University: ObjectType[MainResolver, University] = ObjectType(
    "University",
    () => fields[MainResolver, University](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("city", City, resolve = ctx => ctx.ctx.cityResolver.getCity(ctx.value.cityId))
    )
  )

  lazy val Message: ObjectType[MainResolver, Message] = ObjectType(
    "Message",
    () => fields[MainResolver, Message](
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
