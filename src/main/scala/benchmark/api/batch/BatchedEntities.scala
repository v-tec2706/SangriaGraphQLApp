package benchmark.api.batch

import benchmark.Execution.ex
import benchmark.api.CustomTypesSchema.GQLDate
import benchmark.api.{Arguments, CommonEntities}
import benchmark.entities._
import benchmark.resolver.MessageResolver.batchedMessageResolver
import benchmark.resolver.PersonResolver.batchedPersonResolver
import benchmark.resolver._
import sangria.schema.{DeferredValue, Field, IntType, ListType, ObjectType, StringType, fields}

import java.time.LocalDate

object BatchedEntities {
  lazy val Person: ObjectType[MainResolver, Person] = ObjectType(
    "Person",
    () =>
      fields[MainResolver, Person](
        Field("id", IntType, resolve = _.value.id.intValue()),
        Field("firstName", StringType, resolve = _.value.firstName),
        Field("lastName", StringType, resolve = _.value.lastName),
        Field("gender", StringType, resolve = _.value.gender),
        Field("birthday", GQLDate, resolve = _.value.birthday),
        Field("browserUsed", StringType, resolve = _.value.browserUsed),
        Field("creationDate", GQLDate, resolve = _.value.creationDate),
        Field("email", ListType(StringType), resolve = _.value.email),
        Field("speaks", ListType(StringType), resolve = _.value.speaks),
        Field("locationIP", StringType, resolve = _.value.locationIP),
        Field(
          "messages",
          ListType(Message),
          resolve = ctx =>
            ctx.ctx.messagesResolver
              .getBySender(ctx.value.id)
              .map(_.toSet)
              .map(x => batchedMessageResolver.deferSeq(x.toList))
        ),
        Field("knows", ListType(Person), resolve = ctx => ctx.ctx.personResolver.knows(ctx.value.id).map(batchedPersonResolver.deferSeq)),
        Field("city", City, resolve = ctx => CityResolver.batchedCityResolver.defer(ctx.value.cityId)),
        Field(
          "university",
          University,
          resolve = ctx => {
            DeferredValue(UniversityResolver.batchedUniversityResolver.deferRelSeq(UniversityResolver.universityByStudent, ctx.value.id))
              .map(_.head)
          }
        )
      )
  )

  lazy val PersonWithArgs: ObjectType[MainResolver, Person] = ObjectType(
    "Person",
    () =>
      fields[MainResolver, Person](
        Field("id", IntType, resolve = _.value.id.intValue()),
        Field("firstName", StringType, resolve = _.value.firstName),
        Field("lastName", StringType, resolve = _.value.lastName),
        Field("gender", StringType, resolve = _.value.gender),
        Field("birthday", GQLDate, resolve = _.value.birthday),
        Field("browserUsed", StringType, resolve = _.value.browserUsed),
        Field("creationDate", GQLDate, resolve = _.value.creationDate),
        Field("email", ListType(StringType), resolve = _.value.email),
        Field("speaks", ListType(StringType), resolve = _.value.speaks),
        Field("locationIP", StringType, resolve = _.value.locationIP),
        Field(
          "messages",
          ListType(Message),
          resolve = ctx => ctx.ctx.messagesResolver.getBySender(ctx.value.id).map(batchedMessageResolver.deferSeq)
        ),
        Field(
          "knows",
          ListType(Person),
          arguments = Arguments.Year :: Arguments.Country :: Nil,
          resolve = ctx => {
            for {
              friendIds <- ctx.ctx.personResolver.knows(ctx.value.id)
              workAt <- ctx.ctx.companyResolver
                .workAt(friendIds.toList)
                .map(_.filter(_._3.compareTo(LocalDate.parse(ctx.arg(Arguments.Year).toString + "-01-01")) >= 0))
              country <- ctx.ctx.countryResolver.getCountryByName(ctx.arg(Arguments.Country)).map(_.id)
              companyLocatedIn <- ctx.ctx.companyResolver
                .getCompanies(workAt.map(_._2))
                .map(_.filter(_.countryId == country))
                .map(_.map(_.id))
              toFind = workAt.filter(x => companyLocatedIn.contains(x._2)).map(_._1)
            } yield batchedPersonResolver.deferSeq(toFind.toList)
          }
        ),
        Field("city", City, resolve = ctx => CityResolver.batchedCityResolver.defer(ctx.value.cityId)),
        Field(
          "university",
          University,
          resolve = ctx => ctx.ctx.universityResolver.byStudent(ctx.value.id).map(UniversityResolver.batchedUniversityResolver.defer)
        )
      )
  )

  lazy val City: ObjectType[MainResolver, City] = ObjectType(
    "City",
    () =>
      fields[MainResolver, City](
        Field("id", IntType, resolve = _.value.id.intValue()),
        Field("name", StringType, resolve = _.value.name),
        Field("url", StringType, resolve = _.value.url),
        Field("country", Country, resolve = ctx => CountryResolver.batchedCountryResolver.defer(ctx.value.countryId))
      )
  )

  lazy val Country: ObjectType[MainResolver, Country] = ObjectType(
    "Country",
    () =>
      fields[MainResolver, Country](
        Field("id", IntType, resolve = _.value.id.intValue()),
        Field("name", StringType, resolve = _.value.name),
        Field("url", StringType, resolve = _.value.url),
        Field(
          "continent",
          CommonEntities.Continent,
          resolve = ctx => ContinentResolver.batchedContinentResolver.defer(ctx.value.continentId)
        )
      )
  )

  lazy val University: ObjectType[MainResolver, University] = ObjectType(
    "University",
    () =>
      fields[MainResolver, University](
        Field("id", IntType, resolve = _.value.id.intValue()),
        Field("name", StringType, resolve = _.value.name),
        Field("url", StringType, resolve = _.value.url),
        Field("city", City, resolve = ctx => CityResolver.batchedCityResolver.defer(ctx.value.cityId))
      )
  )

  lazy val Message: ObjectType[MainResolver, Message] = ObjectType(
    "Message",
    () =>
      fields[MainResolver, Message](
        Field("id", IntType, resolve = _.value.id.intValue()),
        Field("country", Country, resolve = ctx => CountryResolver.batchedCountryResolver.defer(ctx.value.countryId)),
        Field("person", Person, resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.value.personId)),
        Field("content", StringType, resolve = _.value.content),
        Field("length", IntType, resolve = _.value.length),
        Field("browserUsed", StringType, resolve = _.value.browserUsed),
        Field("creationDate", GQLDate, resolve = _.value.creationDate),
        Field("locationIP", StringType, resolve = _.value.locationIP)
      )
  )
}
