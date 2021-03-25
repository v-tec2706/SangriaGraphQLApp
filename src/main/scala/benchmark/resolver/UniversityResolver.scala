package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.University
import benchmark.repository.{Repository, UniversityRepository}
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class UniversityResolver(universityRepository: UniversityRepository) extends Resolver {
  def getUniversity(id: Long): Future[University] = Repository.database.run(universityRepository.getUniversity(id)).map(_.head)

  def getUniversities(ids: Seq[Long]): Future[Seq[University]] = Repository.database.run(universityRepository.getUniversities(ids.toList))

  def byStudent(personId: Long): Future[Long] = Repository.database.run(universityRepository.studyAt(personId).map(_.head))
}

object UniversityResolver {
  implicit val hasId: HasId[University, Long] = HasId[University, Long](_.id)
  val batchedUniversityResolver: Fetcher[MainResolver, University, University, Long] = Fetcher((ctx: MainResolver, ids: Seq[Long]) => ctx.universityResolver.getUniversities(ids.toList))
  val cachedUniversityResolver: Fetcher[MainResolver, University, University, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[University, NoStream, Effect.All]] = ids.map(id => ctx.universityResolver.universityRepository.getUniversity(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[University], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    })
  val batchedCachedUniversityResolver: Fetcher[MainResolver, University, University, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.universityResolver.getUniversities(ids)
  )
}