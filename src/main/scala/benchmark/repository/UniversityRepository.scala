package benchmark.repository

import benchmark.data.model.UniversityDb.UniversityRecord
import benchmark.data.model.{StudyAtRelationDb, UniversityDb}
import benchmark.entities.University
import slick.dbio
import slick.dbio.Effect
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.sql.FixedSqlStreamingAction

import scala.concurrent.ExecutionContext.Implicits.global

case class UniversityRepository() extends Repository[UniversityRecord, UniversityDb, University] {
  override def entityMapping: UniversityRecord => University = p => University(p._1, p._2, p._3, p._4)

  def getUniversity(id: Long): dbio.DBIO[Seq[University]] = get { u: UniversityDb => u.id === id }.map(_.map(entity))

  def getUniversities(ids: List[Long]): dbio.DBIO[Seq[University]] = get { u: UniversityDb => u.id inSet ids }.map(_.map(entity))

  def studyAt(personId: Long): FixedSqlStreamingAction[Seq[Long], Long, Effect.Read] =
    StudyAtRelationDb.table.filter(_.personId === personId).map(_.universityId).result

  override def table: H2Profile.api.TableQuery[UniversityDb] = UniversityDb.table
}
