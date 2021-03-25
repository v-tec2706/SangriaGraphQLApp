package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Person
import benchmark.repository.{PersonRepository, Repository}
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class PersonResolver(personRepository: PersonRepository) extends Resolver {
  def getPerson(id: Long): Future[Person] = Repository.database.run(personRepository.getPerson(id).map(_.head))

  def getPersonByName(name: String): Future[Person] = Repository.database.run(personRepository.getPerson(name).map(_.head))

  def getPeopleAsync(ids: List[Long]): Future[Seq[Person]] = Repository.database.run(DBIO.sequence(ids.map(id => personRepository.getPerson(id).map(_.head))))

  def getPeople(ids: List[Long]): Future[Seq[Person]] = Repository.database.run(personRepository.getPeople(ids))

  def knows(id: Long): Future[Seq[Long]] = Repository.database.run(personRepository.knows(id))
}

object PersonResolver {
  implicit val hasId: HasId[Person, Long] = HasId[Person, Long](_.id)
  val batchedPersonResolver: Fetcher[MainResolver, Person, Person, Long] = Fetcher((ctx: MainResolver, ids: Seq[Long]) => ctx.personResolver.getPeople(ids.toList))
  val cachedPersonResolver: Fetcher[MainResolver, Person, Person, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[Person, NoStream, Effect.All]] = ids.map(id => ctx.personResolver.personRepository.getPerson(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[Person], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    })
  val batchedCachedPersonResolver: Fetcher[MainResolver, Person, Person, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.personResolver.getPeople(ids.toList)
  )
}