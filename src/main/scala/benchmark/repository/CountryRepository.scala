package benchmark.repository

import benchmark.Execution.ex
import benchmark.data.model.CountryDb
import benchmark.data.model.CountryDb.CountryRecord
import benchmark.entities.Country
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

case class CountryRepository() extends Repository[CountryRecord, CountryDb, Country] {
  override def table: H2Profile.api.TableQuery[CountryDb] = CountryDb.table

  override def entityMapping: CountryRecord => Country = p => Country(p._1, p._2, p._3, p._4)

  def getCountry(id: Long): dbio.DBIO[Seq[Country]] = get { c: CountryDb => c.id === id }.map(_.map(entity))

  def getCountry(name: String): dbio.DBIO[Seq[Country]] = get { c: CountryDb => c.name === name }.map(_.map(entity))

  def getCountries(ids: List[Long]): dbio.DBIO[Seq[Country]] = get { c: CountryDb => c.id inSet ids }.map(_.map(entity))
}
