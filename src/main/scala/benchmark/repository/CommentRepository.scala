package benchmark.repository

import benchmark.data.model.CommentDb
import benchmark.data.model.CommentDb.CommentRecord
import benchmark.entities.Comment
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class CommentRepository() extends Repository[CommentRecord, CommentDb, Comment] {
  override def table: H2Profile.api.TableQuery[CommentDb] = CommentDb.table

  override def entityMapping: CommentRecord => Comment = p => Comment(p._1, p._2, p._4, p._3, p._8, p._9, p._5, p._6, p._7)

  def getComment(id: Long): dbio.DBIO[Seq[Comment]] = get { c: CommentDb => c.id === id }.map(_.map(entity))

  def getComments(ids: List[Long]): dbio.DBIO[Seq[Comment]] = get { c: CommentDb => c.id inSet ids }.map(_.map(entity))
}
