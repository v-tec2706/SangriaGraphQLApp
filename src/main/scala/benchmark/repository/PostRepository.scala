package benchmark.repository

import benchmark.Execution.ex
import benchmark.data.model.PostDb
import benchmark.data.model.PostDb.PostRecord
import benchmark.entities.Post
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

case class PostRepository() extends Repository[PostRecord, PostDb, Post] {
  override def table: H2Profile.api.TableQuery[PostDb] = PostDb.table

  override def entityMapping: PostRecord => Post = p => Post(p._1, p._2, p._3)

  def getPost(id: Long): dbio.DBIO[Seq[Post]] = get { c: PostDb => c.forumId === id }.map(_.map(entity))

  def getPosts(ids: List[Long]): dbio.DBIO[Seq[Post]] = get { c: PostDb => c.forumId inSet ids }.map(_.map(entity))
}
