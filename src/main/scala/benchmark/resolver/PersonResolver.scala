package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Person
import benchmark.repository.{PersonRepository, Repository}

import scala.concurrent.Future

case class PersonResolver(personRepository: PersonRepository) {
  def getPerson(id: Long): Future[Person] = Repository.database.run(personRepository.getPerson(id).map(_.head))
}
