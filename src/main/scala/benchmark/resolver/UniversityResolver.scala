package benchmark.resolver

import benchmark.Execution.ex
import benchmark.entities.University
import benchmark.repository.Repository.runManySeq
import benchmark.repository.{Repository, UniversityRepository}
import sangria.execution.deferred._
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class UniversityResolver(universityRepository: UniversityRepository) extends Resolver {

  def getUniversitiesByStudents(studentsId: Seq[Long]): Future[Seq[(Seq[Long], Option[University])]] = for {
    universitiesId <- byStudents(studentsId)
    universities <- getUniversities(universitiesId.map(_._2))
  } yield matchStudyRelation(studentsId, universitiesId, universities)

  def byStudents(ids: Seq[Long]): Future[Seq[(Long, Long)]] = {
    println(s"Getting universityId fot students: $ids")
    Repository.database.run(universityRepository.manyStudyAt(ids.toList))
  }

  def getUniversities(ids: Seq[Long]): Future[Seq[University]] = {
    println(s"Fetching universities: $ids")
    Repository.database.run(universityRepository.getUniversities(ids.toList))
  }

  private def matchStudyRelation(
                                  studentsId: Seq[Long],
                                  studyAt: Seq[(Long, Long)],
                                  universities: Seq[University]
                                ): Seq[(Seq[Long], Option[University])] = {
    val studyAtRel: Map[Long, Long] = studyAt.toMap
    val universitiesMap: Map[Long, Seq[University]] = universities.groupBy(_.id)
    studentsId.map(studentId => (Seq(studentId), studyAtRel.get(studentId).flatMap(universitiesMap.get(_).map(_.head))))
  }

  def getUniversitiesByStudentsSeq(studentsId: Seq[Long]): Future[Seq[(Seq[Long], Option[University])]] = {
    for {
      universitiesId <- runManySeq(studentsId.map(universityRepository.studyAt(_).map(_.head)))
      universities <- runManySeq(universitiesId.map(_._2).map(universityRepository.getUniversity(_).map(_.head)))
    } yield matchStudyRelation(studentsId, universitiesId, universities)
  }

  def byStudent(personId: Long): Future[(Long, Long)] = Repository.database.run(universityRepository.studyAt(personId).map(_.head))

  def getUniversity(id: Long): Future[University] = Repository.database.run(universityRepository.getUniversity(id)).map(_.head)
}

object UniversityResolver {

  implicit val hasId: HasId[University, Long] = HasId[University, Long](_.id)

  val universityByStudent: Relation[University, (Seq[Long], Option[University]), Long] =
    Relation[University, (Seq[Long], Option[University]), Long]("university-student", _._1, _._2.orNull)

  val batchedUniversityResolver: Fetcher[MainResolver, University, (Seq[Long], Option[University]), Long] = Fetcher.rel(
    (ctx: MainResolver, ids: Seq[Long]) => ctx.universityResolver.getUniversities(ids.toList),
    (ctx: MainResolver, ids: RelationIds[University]) => ctx.universityResolver.getUniversitiesByStudents(ids(universityByStudent))
  )

  val cachedUniversityResolver: Fetcher[MainResolver, University, (Seq[Long], Option[University]), Long] = Fetcher.rel(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[University, NoStream, Effect.All]] =
        ids.map(id => ctx.universityResolver.universityRepository.getUniversity(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[University], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    },
    fetchRel =
      (ctx: MainResolver, ids: RelationIds[University]) => ctx.universityResolver.getUniversitiesByStudentsSeq(ids(universityByStudent))
  )

  val batchedCachedUniversityResolver: Fetcher[MainResolver, University, (Seq[Long], Option[University]), Long] = Fetcher.rel(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.universityResolver.getUniversities(ids),
    fetchRel =
      (ctx: MainResolver, ids: RelationIds[University]) => ctx.universityResolver.getUniversitiesByStudents(ids(universityByStudent))
  )
}
