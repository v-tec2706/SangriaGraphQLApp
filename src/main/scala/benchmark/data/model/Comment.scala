package benchmark.data.model

import slick.jdbc.H2Profile.api._

import java.time.LocalDate

class Comment(tag: Tag) extends Table[(Long, Long, Long, Long, String, LocalDate, String, String, Int)](tag, "Comment") {
  override def * = (id, countryId, messageId, personId, browserUsed, creationDate, locationIP, content, length)

  def id = column[Long]("id", O.PrimaryKey)

  def browserUsed = column[String]("browserUsed")

  def creationDate = column[LocalDate]("creationDate")

  def locationIP = column[String]("locationIP")

  def content = column[String]("content")

  def length = column[Int]("length")

  def personId = column[Long]("personId")

  def messageId = column[Long]("messageId")

  def countryId = column[Long]("countryId")

  def countryFk = foreignKey("countryFk", countryId, Country.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def personFk = foreignKey("personFk", personId, Person.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def messageFk = foreignKey("messageFk", messageId, Message.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
}
