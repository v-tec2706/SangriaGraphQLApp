package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Person
import benchmark.repository.{PersonRepository, Repository}

import scala.concurrent.Future

case class PersonResolver(personRepository: PersonRepository) {
  def getPerson(id: Long): Future[Person] = Repository.database.run(personRepository.getPerson(id).map(_.head))

  def getPersonByName(name: String): Future[Person] = Repository.database.run(personRepository.getPerson(name).map(_.head))

  def getPeople(ids: List[Long]): Future[Seq[Person]] = Repository.database.run(personRepository.getPeople(ids))

  def knows(id: Long): Future[Seq[Person]] = Repository.database.run(personRepository.knows(id))
}
