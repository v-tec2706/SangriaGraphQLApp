package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.City
import benchmark.repository.{CityRepository, Repository}

import scala.concurrent.Future

case class CityResolver(cityRepository: CityRepository) {
  def getCity(id: Long): Future[City] = {
    println(s"city-id: $id")
    Repository.database.run(cityRepository.getCity(id).map(_.head))
  }
}
