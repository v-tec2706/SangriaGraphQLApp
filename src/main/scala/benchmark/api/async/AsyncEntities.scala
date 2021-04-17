package benchmark.api.async

import benchmark.Execution.ex
import benchmark.api.CustomTypesSchema.GQLDate
import benchmark.api.{Arguments, CommonEntities}
import benchmark.entities._
import benchmark.resolver.MainResolver
import io.circe.Json
import io.circe.generic.semiauto._
import sangria.federation._
import sangria.schema._

import java.time.LocalDate
import scala.concurrent.Future

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

  implicit val decoder: Decoder[Json, PersonArg] = deriveDecoder[PersonArg].decodeJson(_)
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
      Field("messages", ListType(Message), resolve = ctx => for {
        messagesId <- ctx.ctx.messagesResolver.getBySender(ctx.value.id)
        messages <- ctx.ctx.messagesResolver.getMessageAsync(messagesId.toList)
      } yield messages),
      Field("knows", ListType(Person),
        arguments = Arguments.Year :: Arguments.Country :: Nil,
        resolve = ctx => {
          for {
            friendIds <- ctx.ctx.personResolver.knows(ctx.value.id)
            workAt <- Future.sequence(friendIds.map(ctx.ctx.companyResolver.worksAt))
              .map(_.filter { case (_, _, year) => year.compareTo(LocalDate.parse(ctx.arg(Arguments.Year).toString + "-01-01")) >= 0 })
            country <- ctx.ctx.countryResolver.getCountryByName(ctx.arg(Arguments.Country)).map(_.id)
            companyLocatedIn <- Future.sequence(workAt.map(x => ctx.ctx.companyResolver.getCompany(x._2))).map(_.filter(_.countryId == country)).map(_.map(_.id))
            toFind = workAt.filter(x => companyLocatedIn.contains(x._2)).map(_._1)
            friends <- ctx.ctx.personResolver.getPeopleAsync(toFind.toList)
          } yield friends
        }),
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

  def personResolverF(env: MainResolver): EntityResolver[MainResolver, Json] = EntityResolver[MainResolver, Json, Person, PersonArg](
    __typeName = "Person",
    arg => env.personResolver.getPersonBlocking(arg.id)
  )

  case class PersonArg(id: Long)

}
