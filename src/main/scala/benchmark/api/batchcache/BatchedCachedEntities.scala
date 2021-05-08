package benchmark.api.batchcache

import benchmark.Execution.ex
import benchmark.api.CustomTypesSchema.GQLDate
import benchmark.api.{Arguments, CommonEntities}
import benchmark.entities._
import benchmark.resolver.MessageResolver.{batchedCachedMessageResolver, messageBySender}
import benchmark.resolver.PersonResolver.{batchedCachedPersonResolver, knowsRelation}
import benchmark.resolver.UniversityResolver.universityByStudent
import benchmark.resolver._
import sangria.schema.{Field, IntType, ListType, ObjectType, OptionType, StringType, fields}

import java.time.LocalDate

object BatchedCachedEntities {
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
        Field("messages", ListType(Message), resolve = ctx => batchedCachedMessageResolver.deferRelSeq(messageBySender, ctx.value.id)),
        Field("knows", ListType(Person), resolve = ctx => batchedCachedPersonResolver.deferRelSeq(knowsRelation, ctx.value.id)),
        Field("city", City, resolve = ctx => CityResolver.batchedCachedCityResolver.defer(ctx.value.cityId)),
        Field(
          "university",
          OptionType(ListType(University)),
          resolve = ctx => UniversityResolver.batchedCachedUniversityResolver.deferRelSeq(universityByStudent, ctx.value.id)
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
        Field("messages", ListType(Message), resolve = ctx => batchedCachedMessageResolver.deferRelSeq(messageBySender, ctx.value.id)),
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
            } yield batchedCachedPersonResolver.deferSeq(toFind.toList)
          }
        ),
        Field("city", City, resolve = ctx => CityResolver.batchedCachedCityResolver.defer(ctx.value.cityId)),
        Field(
          "university",
          OptionType(ListType(University)),
          resolve = ctx => UniversityResolver.batchedCachedUniversityResolver.deferRelSeq(universityByStudent, ctx.value.id)
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
        Field("country", Country, resolve = ctx => CountryResolver.batchedCachedCountryResolver.defer(ctx.value.countryId))
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
          resolve = ctx => ContinentResolver.batchedCachedContinentResolver.defer(ctx.value.continentId)
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
        Field("city", City, resolve = ctx => CityResolver.batchedCachedCityResolver.defer(ctx.value.cityId))
      )
  )

  lazy val Message: ObjectType[MainResolver, Message] = ObjectType(
    "Message",
    () =>
      fields[MainResolver, Message](
        Field("id", IntType, resolve = _.value.id.intValue()),
        Field("country", Country, resolve = ctx => CountryResolver.batchedCachedCountryResolver.defer(ctx.value.countryId)),
        Field("person", Person, resolve = ctx => PersonResolver.batchedCachedPersonResolver.defer(ctx.value.personId)),
        Field("content", StringType, resolve = _.value.content),
        Field("length", IntType, resolve = _.value.length),
        Field("browserUsed", StringType, resolve = _.value.browserUsed),
        Field("creationDate", GQLDate, resolve = _.value.creationDate),
        Field("locationIP", StringType, resolve = _.value.locationIP)
      )
  )
}
