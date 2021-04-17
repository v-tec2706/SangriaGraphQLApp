package benchmark.api.batchcache

import benchmark.Execution.ex
import benchmark.api.CustomTypesSchema.GQLDate
import benchmark.api.{Arguments, CommonEntities}
import benchmark.entities._
import benchmark.resolver.MessageResolver.batchedCachedMessageResolver
import benchmark.resolver.PersonResolver.batchedCachedPersonResolver
import benchmark.resolver._
import sangria.schema.{Field, IntType, ListType, LongType, ObjectType, StringType, fields}

import java.time.LocalDate

object BatchedCachedEntities {
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
        resolve = ctx => {
          val z = ctx.ctx.messagesResolver.getBySender(ctx.value.id).map(_.toSet)
          z.map(x => batchedCachedMessageResolver.deferSeq(x.toList))
        }),
      Field("knows", ListType(Person),
        resolve = ctx => ctx.ctx.personResolver.knows(ctx.value.id).map(batchedCachedPersonResolver.deferSeq)),
      Field("city", City, resolve = ctx => CityResolver.batchedCachedCityResolver.defer(ctx.value.cityId)),
      Field("university", University, resolve = ctx => ctx.ctx.universityResolver.byStudent(ctx.value.id).map(UniversityResolver.batchedUniversityResolver.defer)),
    )
  )

  lazy val PersonWithArgs: ObjectType[MainResolver, Person] = ObjectType(
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
        resolve = ctx => ctx.ctx.messagesResolver.getBySender(ctx.value.id).map(batchedCachedMessageResolver.deferSeq)),
      Field("knows", ListType(Person),
        arguments = Arguments.Year :: Arguments.Country :: Nil,
        resolve = ctx => {
          for {
            friendIds <- ctx.ctx.personResolver.knows(ctx.value.id)
            workAt <- ctx.ctx.companyResolver.workAt(friendIds.toList)
              .map(_.filter(_._3.compareTo(LocalDate.parse(ctx.arg(Arguments.Year).toString + "-01-01")) >= 0))
            country <- ctx.ctx.countryResolver.getCountryByName(ctx.arg(Arguments.Country)).map(_.id)
            companyLocatedIn <- ctx.ctx.companyResolver.getCompanies(workAt.map(_._2)).map(_.filter(_.countryId == country)).map(_.map(_.id))
            toFind = workAt.filter(x => companyLocatedIn.contains(x._2)).map(_._1)
          } yield batchedCachedPersonResolver.deferSeq(toFind.toList)
        }),
      Field("city", City, resolve = ctx => CityResolver.batchedCachedCityResolver.defer(ctx.value.cityId)),
      Field("university", University, resolve = ctx => ctx.ctx.universityResolver.byStudent(ctx.value.id).map(UniversityResolver.batchedCachedUniversityResolver.defer)),
    )
  )

  lazy val City: ObjectType[MainResolver, City] = ObjectType(
    "City",
    () => fields[MainResolver, City](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("country", Country, resolve = ctx => CountryResolver.batchedCachedCountryResolver.defer(ctx.value.countryId))
    )
  )

  lazy val Country: ObjectType[MainResolver, Country] = ObjectType(
    "Country",
    () => fields[MainResolver, Country](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("continent", CommonEntities.Continent, resolve = ctx => ContinentResolver.batchedCachedContinentResolver.defer(ctx.value.continentId))
    )
  )

  lazy val University: ObjectType[MainResolver, University] = ObjectType(
    "University",
    () => fields[MainResolver, University](
      Field("id", LongType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
      Field("city", City, resolve = ctx => CityResolver.batchedCachedCityResolver.defer(ctx.value.cityId))
    )
  )

  lazy val Message: ObjectType[MainResolver, Message] = ObjectType(
    "Message",
    () => fields[MainResolver, Message](
      Field("id", LongType, resolve = _.value.id),
      Field("country", Country, resolve = ctx => CountryResolver.batchedCachedCountryResolver.defer(ctx.value.countryId)),
      Field("person", Person, resolve = ctx => PersonResolver.batchedCachedPersonResolver.defer(ctx.value.personId)),
      Field("content", StringType, resolve = _.value.content),
      Field("length", IntType, resolve = _.value.length),
      Field("browserUsed", StringType, resolve = _.value.browserUsed),
      Field("creationDate", GQLDate, resolve = _.value.creationDate),
      Field("locationIP", StringType, resolve = _.value.locationIP),
    )
  )
}