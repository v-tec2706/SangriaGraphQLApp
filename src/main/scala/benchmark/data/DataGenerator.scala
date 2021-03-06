package benchmark.data

import benchmark.data.Insert.RelationRecord
import benchmark.data.model.CityDb.CityRecord
import benchmark.data.model.CompanyDb.CompanyRecord
import benchmark.data.model.ContinentDb.ContinentRecord
import benchmark.data.model.CountryDb.CountryRecord
import benchmark.data.model.ForumDb.ForumRecord
import benchmark.data.model.MessageDb.MessageRecord
import benchmark.data.model.PersonDb.PersonRecord
import benchmark.data.model.PostDb.PostRecord
import benchmark.data.model.TagClassDb.TagClassRecord
import benchmark.data.model.TopicDb.TopicRecord
import benchmark.data.model.UniversityDb.UniversityRecord
import benchmark.data.model._
import benchmark.repository.Repository
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
    DBIO.seq(table.schema.createIfNotExists, table ++= items)
  }
}

object DataGenerator extends App {

  lazy val idRanges: Map[DataGenerator.Entries.Value, Int] = Map(
    Entries.Continent -> 5,
    Entries.Country -> 10,
    Entries.City -> 50,
    Entries.Company -> 20,
    Entries.Person -> 30,
    Entries.Forum -> 30,
    Entries.Message -> 30,
    Entries.Post -> 40,
    Entries.TagClass -> 40,
    Entries.Topic -> 10,
    Entries.University -> 20
  )
  lazy val relationsSize: Map[DataGenerator.Relations.Value, Int] = Map(
    Relations.ForumTagRelation -> 10,
    Relations.HasMemberRelation -> 10,
    Relations.KnowsRelation -> 20,
    Relations.LikesRelation -> 10,
    Relations.MessageTagRelation -> 10,
    Relations.StudyAtRelation -> 1,
    Relations.WorkAtRelation -> 1
  )
  val scalingFactor = 0.1

  lazy val continentInsert = Insert.relationInsert[ContinentRecord, ContinentDb](
    (1 to idRanges(Entries.Continent)) map (i => (i, s"continent-${i}", s"url-${i}")),
    ContinentDb.table
  )
  lazy val countryInsert = Insert.relationInsert[CountryRecord, CountryDb](
    (1 to idRanges(Entries.Country)) map (i => (i, Random.between(1, idRanges(Entries.Continent)), s"country-${i}", s"url-${i}")),
    CountryDb.table
  )
  lazy val cityInsert = Insert.relationInsert[CityRecord, CityDb](
    (1 to idRanges(Entries.City)) map (i => (i, Random.between(1, idRanges(Entries.Country)), s"city-${i}", s"url-${i}")),
    CityDb.table
  )
  lazy val companyInsert = Insert.relationInsert[CompanyRecord, CompanyDb](
    (1 to idRanges(Entries.Company)) map (i => (i, Random.between(1, idRanges(Entries.Country)), s"company-${i}", s"url-${i}")),
    CompanyDb.table
  )
  lazy val personInsert = Insert.relationInsert[PersonRecord, PersonDb](
    (1 to idRanges(Entries.Person)) map (i =>
      (
        i,
        Random.between(1, idRanges(Entries.City)),
        s"first-name-${i}",
        s"last-name-${i}",
        s"genders-$i",
        LocalDate.now().minusYears((i / 50) + 20),
        s"browser-used-$i",
        LocalDate.now().minusMonths(i),
        List(s"email-$i"),
        List(s"speaks-$i"),
        s"locationIp-$i"
      )
      ),
    PersonDb.table
  )
  lazy val forumInsert = Insert.relationInsert[ForumRecord, ForumDb](
    (1 to idRanges(Entries.Forum)) map (i =>
      (i, Random.between(1, idRanges(Entries.Person)), s"title-${i}", LocalDate.now().minusMonths(Random.nextInt(i)))
      ),
    ForumDb.table
  )
  lazy val messageInsert = Insert.relationInsert[MessageRecord, MessageDb](
    messagesGen(),
    MessageDb.table
  )
  lazy val postInsert = Insert.relationInsert[PostRecord, PostDb](
    (1 to idRanges(Entries.Post)) map (i =>
      (Random.between(1, idRanges(Entries.Forum)), List(s"email_1-$i", s"email_2-$i"), List(s"image_file_1-$i", s"image_file_2-$i"))
      ),
    PostDb.table
  )
  lazy val tagClassInsert = Insert.relationInsert[TagClassRecord, TagClassDb](
    (1 to idRanges(Entries.TagClass)) map (i => (i, s"name-$i", s"url-$i")),
    TagClassDb.table
  )
  lazy val topicInsert =
    Insert.relationInsert[TopicRecord, TopicDb]((1 to idRanges(Entries.Topic)) map (i => (i, s"name-$i", s"url-$i")), TopicDb.table)
  lazy val universityInsert = Insert.relationInsert[UniversityRecord, UniversityDb](
    (1 to idRanges(Entries.University)) map (i => (i, Random.between(1, idRanges(Entries.City)), s"name-$i", s"url-$i")),
    UniversityDb.table
  )

  lazy val forumTagRelation = Insert.relationInsert[RelationRecord, ForumTagRelationDb](
    generateRelation(idRanges(Entries.Forum), idRanges(Entries.TagClass), relationsSize(Relations.ForumTagRelation)),
    ForumTagRelationDb.table
  )
  lazy val hasMemberRelation = Insert.relationInsert[RelationRecord, HasMemberRelationDb](
    generateRelation(idRanges(Entries.Forum), idRanges(Entries.Person), relationsSize(Relations.HasMemberRelation)),
    HasMemberRelationDb.table
  )
  lazy val knowsRelation = Insert.relationInsert[RelationRecord, KnowsRelationDb](
    generateRelation(idRanges(Entries.Person), idRanges(Entries.Person), relationsSize(Relations.KnowsRelation)),
    KnowsRelationDb.table
  )
  lazy val likesRelation = Insert.relationInsert[RelationRecord, LikesRelationDb](
    generateRelation(idRanges(Entries.Person), idRanges(Entries.Message), relationsSize(Relations.LikesRelation)),
    LikesRelationDb.table
  )
  lazy val messageTagRelation = Insert.relationInsert[RelationRecord, MessageTagRelationDb](
    generateRelation(idRanges(Entries.Message), idRanges(Entries.TagClass), relationsSize(Relations.MessageTagRelation)),
    MessageTagRelationDb.table
  )
  lazy val studyAtRelation = Insert.relationInsert[RelationRecord, StudyAtRelationDb](
    generateRelation(idRanges(Entries.Person), idRanges(Entries.University), relationsSize(Relations.StudyAtRelation)),
    StudyAtRelationDb.table
  )
  lazy val workAtRelation = Insert.relationInsert[(Long, Long, LocalDate), WorkAtRelationDb](
    generateRelation(idRanges(Entries.Person), idRanges(Entries.Company), relationsSize(Relations.WorkAtRelation)).map(x =>
      (x._1, x._2, LocalDate.now().minusMonths(Random.nextInt(100)))
    ),
    WorkAtRelationDb.table
  )

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

  def generateRelation(maxA: Int, maxB: Int, n: Int = 1): Seq[(Long, Long)] =
    (1 to maxA).flatMap(i => (1 to n).map(_ => i.toLong).zip(Random.shuffle((1 to maxB).toList).take(n).map(_.toLong)))

  def messagesGen() = {
    val values = (1 to idRanges(Entries.Person)).flatMap(i => {
      (1 to idRanges(Entries.Message)).map(_ =>
        (
          Random.between(1, idRanges(Entries.Country)).toLong,
          i.toLong,
          s"browser-used-${Random.nextInt(1000)}",
          LocalDate.now().minusMonths(Random.nextInt(i)),
          s"location-ip-${Random.nextInt(1000)}",
          s"content-${Random.nextInt(1000)}",
          Random.nextInt(i * 1000)
        )
      )
    })
    val ids = (1 to idRanges(Entries.Person) * idRanges(Entries.Country)).toList.map(_.toLong)
    ids.zip(values).map(x => (x._1, x._2._1, x._2._2, x._2._3, x._2._4, x._2._5, x._2._6, x._2._7))
  }

  object Entries extends Enumeration {
    type Entry = Value
    val Continent, Country, City, Company, Person, Forum, Message, Post, TagClass, Topic, University = Value
  }

  Await.ready(insertQuery, Duration.Inf)

  object Relations extends Enumeration {
    type Relation = Value
    val ForumTagRelation, HasMemberRelation, KnowsRelation, LikesRelation, MessageTagRelation, StudyAtRelation, WorkAtRelation = Value
  }

}
