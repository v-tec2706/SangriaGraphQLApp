package benchmark.repository

import benchmark.data.model.TagClassDb
import benchmark.data.model.TagClassDb.TagClassRecord
import benchmark.entities.TagClass
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class TagClassRepository() extends Repository[TagClassRecord, TagClassDb, TagClass] {
  override def table: H2Profile.api.TableQuery[TagClassDb] = TagClassDb.table

  override def entityMapping: TagClassRecord => TagClass = p => TagClass(p._1, p._2, p._3)

  def getTagClass(id: Long): dbio.DBIO[Seq[TagClass]] = get { c: TagClassDb => c.id === id }.map(_.map(entity))

  def getTags(ids: List[Long]): dbio.DBIO[Seq[TagClass]] = get { c: TagClassDb => c.id inSet ids }.map(_.map(entity))
}
