package benchmark.repository

import benchmark.data.model.UniversityDb.UniversityRecord
import benchmark.data.model.{StudyAtRelationDb, UniversityDb}
import benchmark.entities.University
import slick.dbio
import slick.dbio.Effect
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class UniversityRepository() extends Repository[UniversityRecord, UniversityDb, University] {
  override def entityMapping: UniversityRecord => University = p => University(p._1, p._2, p._3, p._4)

  def getUniversity(id: Long): dbio.DBIO[Seq[University]] = get { u: UniversityDb => u.id === id }.map(_.map(entity))

  def getUniversities(ids: List[Long]): dbio.DBIO[Seq[University]] = get { u: UniversityDb => u.id inSet ids }.map(_.map(entity))

  def studyAt(personId: Long): DBIOAction[Seq[University], NoStream, Effect.Read] = (for {
    studyRel <- StudyAtRelationDb.table
    university <- table if studyRel.personId === personId && university.id === studyRel.universityId
  } yield university).result.map(_.map(x => University(x._1, x._2, x._3, x._4)))

  override def table: H2Profile.api.TableQuery[UniversityDb] = UniversityDb.table
}
