package benchmark.data.model

import benchmark.data.model.MessageDb.MessageRecord
import slick.jdbc.H2Profile.api._

import java.time.LocalDate

class MessageDb(tag: Tag) extends Table[MessageRecord](tag, "Message") {
  override def * = (id, countryId, personId, browserUsed, creationDate, locationIP, content, length)

  def id = column[Long]("id", O.PrimaryKey)

  def browserUsed = column[String]("browserUsed")

  def creationDate = column[LocalDate]("creationDate")

  def locationIP = column[String]("locationIP")

  def content = column[String]("content")

  def length = column[Int]("length")

  def personId = column[Long]("personId")

  def countryFk = foreignKey("countryFk", countryId, CountryDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def countryId = column[Long]("countryId")

  def personFk = foreignKey("personFk", personId, PersonDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

}

object MessageDb {
  type MessageRecord = (Long, Long, Long, String, LocalDate, String, String, Int)
  val table = TableQuery[MessageDb]
}
