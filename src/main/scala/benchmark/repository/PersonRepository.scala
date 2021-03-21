package benchmark.repository

import benchmark.data.model.PersonDb
import benchmark.data.model.PersonDb.PersonRecord
import benchmark.entities.Person
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class PersonRepository() extends Repository[PersonRecord, PersonDb, Person] {
  override def table: H2Profile.api.TableQuery[PersonDb] = PersonDb.table

  override def entityMapping: PersonRecord => Person = p => Person(p._1, p._2, p._3, p._4, p._5, p._6, p._7, p._8, p._9, p._10, p._11)

  def getPerson(id: Long): dbio.DBIO[Seq[Person]] = get { p: PersonDb => p.id === id }.map(_.map(entity))

  def getPeople(ids: List[Long]): dbio.DBIO[Seq[Person]] = get { p: PersonDb => p.id inSet ids }.map(_.map(entity))
}
