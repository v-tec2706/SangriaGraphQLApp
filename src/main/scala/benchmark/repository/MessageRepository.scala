package benchmark.repository

import benchmark.data.model.MessageDb
import benchmark.data.model.MessageDb.MessageRecord
import benchmark.entities.Message
import slick.dbio
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class MessageRepository() extends Repository[MessageRecord, MessageDb, Message] {
  override def table: H2Profile.api.TableQuery[MessageDb] = MessageDb.table

  override def entityMapping: MessageRecord => Message = p => Message(p._1, p._2, p._3, p._7, p._8, p._4, p._5, p._6)

  def getMessage(id: Long): dbio.DBIO[Seq[Message]] = get { m: MessageDb => m.id === id }.map(_.map(entity))

  def getMessages(ids: List[Long]): dbio.DBIO[Seq[Message]] = get { m: MessageDb => m.id inSet ids }.map(_.map(entity))
}
