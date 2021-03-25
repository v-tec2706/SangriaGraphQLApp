package benchmark.repository

import benchmark.data.model.PersonDb.PersonRecord
import benchmark.data.model.{KnowsRelationDb, PersonDb}
import benchmark.entities.Person
import slick.dbio
import slick.dbio.Effect
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class PersonRepository() extends Repository[PersonRecord, PersonDb, Person] {
  def getPerson(id: Long): dbio.DBIO[Seq[Person]] = get { p: PersonDb => p.id === id }.map(_.map(entity))

  def getPerson(name: String): dbio.DBIO[Seq[Person]] = get { p: PersonDb => p.firstName === name }.map(_.map(entity))

  def getPeople(ids: List[Long]): dbio.DBIO[Seq[Person]] = get { p: PersonDb => p.id inSet ids }.map(_.map(entity))

  def knows(personId: Long): DBIOAction[Seq[Long], NoStream, Effect.Read] = KnowsRelationDb.table.filter(_.personId === personId).map(_.friendId).result

  override def table: H2Profile.api.TableQuery[PersonDb] = PersonDb.table

  override def entityMapping: PersonRecord => Person = p => Person(p._1, p._2, p._3, p._4, p._5, p._6, p._7, p._8, p._9, p._10, p._11)
}
