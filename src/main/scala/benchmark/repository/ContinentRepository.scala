package benchmark.repository

import benchmark.data.model.ContinentDb
import benchmark.data.model.ContinentDb.ContinentRecord
import benchmark.entities.Continent
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class ContinentRepository() extends Repository[ContinentRecord, ContinentDb, Continent] {
  override def table: H2Profile.api.TableQuery[ContinentDb] = ContinentDb.table

  override def entityMapping: ContinentRecord => Continent = p => Continent(p._1, p._2, p._3)

  def getContinent(id: Long): dbio.DBIO[Seq[Continent]] = get { c: ContinentDb => c.id === id }.map(_.map(entity))

  def getContinents(ids: List[Long]): dbio.DBIO[Seq[Continent]] = get { c: ContinentDb => c.id inSet ids }.map(_.map(entity))
}
