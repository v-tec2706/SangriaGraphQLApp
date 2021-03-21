package benchmark.repository

import benchmark.data.model.ForumDb
import benchmark.data.model.ForumDb.ForumRecord
import benchmark.entities.Forum
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class ForumRepository() extends Repository[ForumRecord, ForumDb, Forum] {
  override def table: H2Profile.api.TableQuery[ForumDb] = ForumDb.table

  override def entityMapping: ForumRecord => Forum = f => Forum(f._1, f._2, f._3, f._4)

  def getForum(id: Long): dbio.DBIO[Seq[Forum]] = get { f: ForumDb => f.id === id }.map(_.map(entity))

  def getForums(ids: List[Long]): dbio.DBIO[Seq[Forum]] = get { f: ForumDb => f.id inSet ids }.map(_.map(entity))
}
