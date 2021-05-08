package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Person
import benchmark.repository.Repository.runManySeq
import benchmark.repository.{PersonRepository, Repository}
import sangria.execution.deferred._
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

case class PersonResolver(personRepository: PersonRepository) extends Resolver {
  def getPersonBlocking(id: Long): Option[Person] = Await.result(getPerson(id).map(Some(_)), 3.seconds)

  def getPerson(id: Long): Future[Person] = Repository.database.run(personRepository.getPerson(id).map(_.head))

  def getPersonByName(name: String): Future[Person] = Repository.database.run(personRepository.getPerson(name).map(_.head))

  def getPeopleAsync(ids: List[Long]): Future[Seq[Person]] =
    Repository.database.run(DBIO.sequence(ids.map(id => personRepository.getPerson(id).map(_.head))))

  def knows(id: Long): Future[Seq[Long]] = Repository.database.run(personRepository.knows(id)).map(_.map(_._2))

  def getFriendsOfPeople(peopleIds: Seq[Long]): Future[Seq[(Seq[Long], Person)]] = for {
    knowsRelation <- manyKnows(peopleIds.toList)
    friends <- getPeople(knowsRelation.map(_._2).toList)
  } yield matchFriendsRelation(peopleIds, knowsRelation, friends)

  def getPeople(ids: List[Long]): Future[Seq[Person]] = Repository.database.run(personRepository.getPeople(ids))

  def manyKnows(ids: List[Long]): Future[Seq[(Long, Long)]] = Repository.database.run(personRepository.manyKnows(ids))

  def getFriendsOfPeopleSeq(peopleIds: Seq[Long]): Future[Seq[(Seq[Long], Person)]] = for {
    knowsRelation <- runManySeq(peopleIds.map(personRepository.knows))
    friends <- runManySeq(knowsRelation.flatten.map(x => personRepository.getPerson(x._2)))
  } yield matchFriendsRelation(peopleIds, knowsRelation.flatten, friends.flatten)

  private def matchFriendsRelation(
                                    peopleId: Seq[Long],
                                    knowsRelation: Seq[(Long, Long)],
                                    friends: Seq[Person]
                                  ): Seq[(Seq[Long], Person)] = {
    val knows: Map[Long, Seq[Long]] = knowsRelation.groupBy(_._1).view.mapValues(_.map(_._2)).toMap
    val friendsMap: Map[Long, Person] = friends.groupBy(_.id).view.mapValues(_.head).toMap
    peopleId.flatMap(personId => {
      val friends = knows(personId).map(friendsMap)
      friends.map((Seq(personId), _))
    })
  }
}

object PersonResolver {

  implicit val hasId: HasId[Person, Long] = HasId[Person, Long](_.id)

  val knowsRelation: Relation[Person, (Seq[Long], Person), Long] =
    Relation[Person, (Seq[Long], Person), Long]("person-person", _._1, _._2)

  val batchedPersonResolver: Fetcher[MainResolver, Person, (Seq[Long], Person), Long] =
    Fetcher.rel(
      fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.personResolver.getPeople(ids.toList),
      fetchRel = (ctx: MainResolver, ids: RelationIds[Person]) => ctx.personResolver.getFriendsOfPeople(ids(knowsRelation))
    )

  val cachedPersonResolver = Fetcher.rel(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[Person, NoStream, Effect.All]] = ids.map(id => ctx.personResolver.personRepository.getPerson(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[Person], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    },
    fetchRel = (ctx: MainResolver, ids: RelationIds[Person]) => ctx.personResolver.getFriendsOfPeopleSeq(ids(knowsRelation))
  )

  val batchedCachedPersonResolver: Fetcher[MainResolver, Person, (Seq[Long], Person), Long] = Fetcher.rel(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.personResolver.getPeople(ids.toList),
    fetchRel = (ctx: MainResolver, ids: RelationIds[Person]) => ctx.personResolver.getFriendsOfPeople(ids(knowsRelation))
  )
}
