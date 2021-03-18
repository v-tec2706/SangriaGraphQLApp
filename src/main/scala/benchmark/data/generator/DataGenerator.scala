package benchmark.data.generator

import benchmark.data.generator.Insert.RelationRecord
import benchmark.data.model.City.CityRecord
import benchmark.data.model.Company.CompanyRecord
import benchmark.data.model.Continent.ContinentRecord
import benchmark.data.model.Country.CountryRecord
import benchmark.data.model.Forum.ForumRecord
import benchmark.data.model.Message.MessageRecord
import benchmark.data.model.Person.PersonRecord
import benchmark.data.model.Post.PostRecord
import benchmark.data.model.TagClass.TagClassRecord
import benchmark.data.model.Topic.TopicRecord
import benchmark.data.model.University.UniversityRecord
import benchmark.data.model._
import database.Repository
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Random, Success, Try}

object Insert {
  type RelationRecord = (Long, Long)

  def relationInsert[S, A <: Table[S]](items: Seq[S], table: TableQuery[A]): DBIOAction[Unit, NoStream, Effect.Schema with Effect.Write] = {
    table.schema.createStatements.foreach(println)
    DBIO.seq(
      table.schema.createIfNotExists,
      table ++= items)
  }
}

object DataGenerator extends App {

  lazy val idRanges: Map[DataGenerator.Entries.Value, Int] = Map(
    Entries.Continent -> 5,
    Entries.Country -> 10,
    Entries.City -> 50,
    Entries.Company -> 200,
    Entries.Person -> 1000,
    Entries.Forum -> 100,
    Entries.Message -> 2000,
    Entries.Post -> 4000,
    Entries.TagClass -> 4000,
    Entries.Topic -> 1000,
    Entries.University -> 200
  )

  lazy val continentInsert = Insert.relationInsert[ContinentRecord, Continent]((1 to idRanges(Entries.Continent)) map (i => (i, s"continent-${i}", s"url-${i}")), Continent.table)
  lazy val countryInsert = Insert.relationInsert[CountryRecord, Country]((1 to idRanges(Entries.Country)) map (i => (i, Random.nextInt(idRanges(Entries.Continent)), s"country-${i}", s"url-${i}")), Country.table)
  lazy val cityInsert = Insert.relationInsert[CityRecord, City]((1 to idRanges(Entries.City)) map (i => (i, Random.nextInt(idRanges(Entries.Country)), s"city-${i}", s"url-${i}")), City.table)
  lazy val companyInsert = Insert.relationInsert[CompanyRecord, Company]((1 to idRanges(Entries.Company)) map (i => (i, Random.nextInt(idRanges(Entries.City)), s"company-${i}", s"url-${i}")), Company.table)
  lazy val personInsert = Insert.relationInsert[PersonRecord, Person]((1 to idRanges(Entries.Person)) map (i => (i, Random.nextInt(idRanges(Entries.City)), s"first-name-${i}", s"last-name-${i}", s"genders-$i", LocalDate.now().minusYears((i / 50) + 20), s"browser-used-$i", LocalDate.now().minusMonths(i), List(s"email-$i"), List(s"speaks-$i"), s"locationIp-$i")), Person.table)
  lazy val forumInsert = Insert.relationInsert[ForumRecord, Forum]((1 to idRanges(Entries.Forum)) map (i => (i, Random.nextInt(idRanges(Entries.Person)), s"title-${i}", LocalDate.now().minusMonths(Random.nextInt(i)))), Forum.table)
  lazy val messageInsert = Insert.relationInsert[MessageRecord, Message]((1 to idRanges(Entries.Message)) map (i => (i, Random.nextInt(idRanges(Entries.Person)), Random.nextInt(idRanges(Entries.Person)), s"browser-used-$i", LocalDate.now().minusMonths(Random.nextInt(i)), s"location-ip-${i}", s"content-$i", Random.nextInt(i * 1000))), Message.table)
  lazy val postInsert = Insert.relationInsert[PostRecord, Post]((1 to idRanges(Entries.Post)) map (i => (Random.nextInt(idRanges(Entries.Forum)), List(s"email_1-$i", s"email_2-$i"), List(s"image_file_1-$i", s"image_file_2-$i"))), Post.table)
  lazy val tagClassInsert = Insert.relationInsert[TagClassRecord, TagClass]((1 to idRanges(Entries.TagClass)) map (i => (i, s"name-$i", s"url-$i")), TagClass.table)
  lazy val topicInsert = Insert.relationInsert[TopicRecord, Topic]((1 to idRanges(Entries.Topic)) map (i => (i, s"name-$i", s"url-$i")), Topic.table)
  lazy val universityInsert = Insert.relationInsert[UniversityRecord, University]((1 to idRanges(Entries.University)) map (i => (i, Random.nextInt(idRanges(Entries.City)), s"name-$i", s"url-$i")), University.table)

  lazy val forumTagRelation = Insert.relationInsert[RelationRecord, ForumTagRelation](generateRelation(idRanges(Entries.Forum), idRanges(Entries.TagClass), 200), ForumTagRelation.table)
  lazy val hasMemberRelation = Insert.relationInsert[RelationRecord, HasMemberRelation](generateRelation(idRanges(Entries.Forum), idRanges(Entries.Person), 200), HasMemberRelation.table)
  lazy val knowsRelation = Insert.relationInsert[RelationRecord, KnowsRelation](generateRelation(idRanges(Entries.Person), idRanges(Entries.Person), 200), KnowsRelation.table)
  lazy val likesRelation = Insert.relationInsert[RelationRecord, LikesRelation](generateRelation(idRanges(Entries.Person), idRanges(Entries.Message), 200), LikesRelation.table)
  lazy val messageTagRelation = Insert.relationInsert[RelationRecord, MessageTagRelation](generateRelation(idRanges(Entries.Message), idRanges(Entries.TagClass), 200), MessageTagRelation.table)
  lazy val studyAtRelation = Insert.relationInsert[RelationRecord, StudyAtRelation](generateRelation(idRanges(Entries.Person), idRanges(Entries.University), 200), StudyAtRelation.table)
  lazy val workAtRelation = Insert.relationInsert[RelationRecord, WorkAtRelation](generateRelation(idRanges(Entries.Person), idRanges(Entries.Company), 200), WorkAtRelation.table)

  val db = Repository.database
  val insertQueries = DBIO.seq(
    continentInsert,
    countryInsert,
    cityInsert,
    companyInsert,
    personInsert,
    forumInsert,
    messageInsert,
    postInsert,
    tagClassInsert,
    topicInsert,
    universityInsert,
    forumTagRelation,
    hasMemberRelation,
    knowsRelation,
    likesRelation,
    messageTagRelation,
    studyAtRelation,
    workAtRelation
  )

  val insertQuery = db.run(insertQueries)

  insertQuery onComplete handle()

  def handle[T](): Try[T] => Unit = {
    case Success(_) => println("Ok")
    case Failure(exception) => println(s"Failure: $exception")
  }

  def generateRelation(maxA: Int, maxB: Int, n: Int): Seq[(Long, Long)] = {
    Random.shuffle((1 to n).map(_ => (Random.nextLong(maxA), Random.nextLong(maxB))) ++
      (1 to n).map(_ => (Random.nextLong(maxB), Random.nextLong(maxA))))
  }

  object Entries extends Enumeration {
    type Entry = Value
    val Continent, Country, City, Company, Person, Forum, Message, Post, TagClass, Topic, University = Value
  }

  Await.ready(insertQuery, Duration.Inf)
}