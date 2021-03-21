package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Country
import benchmark.repository.{CountryRepository, Repository}

import scala.concurrent.Future

case class CountryResolver(countryRepository: CountryRepository) {
  def getCountry(id: Long): Future[Country] = Repository.database.run(countryRepository.getCountry(id).map(_.head))
}
