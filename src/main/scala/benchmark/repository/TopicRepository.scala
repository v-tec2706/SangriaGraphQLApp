package benchmark.repository

import benchmark.data.model.TopicDb
import benchmark.data.model.TopicDb.TopicRecord
import benchmark.entities.Topic
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class TopicRepository() extends Repository[TopicRecord, TopicDb, Topic] {
  override def table: H2Profile.api.TableQuery[TopicDb] = TopicDb.table

  override def entityMapping: TopicRecord => Topic = p => Topic(p._1, p._2, p._3)

  def getCity(id: Long): dbio.DBIO[Seq[Topic]] = get { c: TopicDb => c.id === id }.map(_.map(entity))

  def getCities(ids: List[Long]): dbio.DBIO[Seq[Topic]] = get { c: TopicDb => c.id inSet ids }.map(_.map(entity))
}
