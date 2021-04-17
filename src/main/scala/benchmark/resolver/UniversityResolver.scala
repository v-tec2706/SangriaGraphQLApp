package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.University
import benchmark.repository.{Repository, UniversityRepository}
import sangria.execution.deferred._
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class UniversityResolver(universityRepository: UniversityRepository) extends Resolver {

  def getUniversityByStudent(s: Seq[Long]): Future[Seq[(Seq[Long], University)]] = {
    def getUniversityByStudent_(studentsIds: Seq[Long]): Seq[Future[(Seq[Long], University)]] = for {
      universitiesIds <- studentsIds
        .map(s => (s, byStudent(s)))
        .map(x => {
          for {
            b <- x._2
            c <- getUniversity(b)
          } yield (Seq(x._1), c)
        })
    } yield universitiesIds

    Future.sequence(getUniversityByStudent_(s))
  }

  def byStudent(personId: Long): Future[Long] = Repository.database.run(universityRepository.studyAt(personId).map(_.head))

  def getUniversity(id: Long): Future[University] = Repository.database.run(universityRepository.getUniversity(id)).map(_.head)

  def getUniversities(ids: Seq[Long]): Future[Seq[University]] = Repository.database.run(universityRepository.getUniversities(ids.toList))
}

object UniversityResolver {
  val universityByStudent: Relation[University, (Seq[Long], University), Long] =
    Relation[University, (Seq[Long], University), Long]("university-student", _._1, _._2)

  implicit val hasId: HasId[University, Long] = HasId[University, Long](_.id)
  val batchedUniversityResolver: Fetcher[MainResolver, University, (Seq[Long], University), Long] = Fetcher.rel(
    (ctx: MainResolver, ids: Seq[Long]) => ctx.universityResolver.getUniversities(ids.toList),
    (ctx: MainResolver, ids: RelationIds[University]) => ctx.universityResolver.getUniversityByStudent(ids(universityByStudent))
  )

  val cachedUniversityResolver: Fetcher[MainResolver, University, University, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[University, NoStream, Effect.All]] =
        ids.map(id => ctx.universityResolver.universityRepository.getUniversity(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[University], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    }
  )
  val batchedCachedUniversityResolver: Fetcher[MainResolver, University, University, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.universityResolver.getUniversities(ids)
  )
}
