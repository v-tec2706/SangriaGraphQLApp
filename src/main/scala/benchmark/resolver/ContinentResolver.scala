package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Continent
import benchmark.repository.{ContinentRepository, Repository}

import scala.concurrent.Future

case class ContinentResolver(continentRepository: ContinentRepository) {
  def getContinent(id: Long): Future[Continent] = Repository.database.run(continentRepository.getContinent(id).map(_.head))
}
