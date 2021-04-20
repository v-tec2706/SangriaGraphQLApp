package benchmark.repository

import benchmark.Execution.ex
import benchmark.data.model.CityDb
import benchmark.data.model.CityDb.CityRecord
import benchmark.entities.City
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

case class CityRepository() extends Repository[CityRecord, CityDb, City] {
  override def table: H2Profile.api.TableQuery[CityDb] = CityDb.table

  override def entityMapping: CityRecord => City = p => City(p._1, p._2, p._3, p._4)

  def getCity(id: Long): dbio.DBIO[Seq[City]] = get { c: CityDb => c.id === id }.map(_.map(entity))

  def getCities(ids: List[Long]): dbio.DBIO[Seq[City]] = get { c: CityDb => c.id inSet ids }.map(_.map(entity))
}
