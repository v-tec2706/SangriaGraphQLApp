package benchmark.data.model

import benchmark.data.model.PersonDb.PersonRecord
import database.PostgresProfile$.api._

import java.time.LocalDate

class PersonDb(tag: Tag) extends slick.jdbc.H2Profile.api.Table[PersonRecord](tag, "Person") {
  override def * = (id, cityId, firstName, lastName, gender, birthday, browserUsed, creationDate, email, speaks, locationIP)

  def id = column[Long]("id", O.PrimaryKey)

  def firstName = column[String]("firsName")

  def lastName = column[String]("lastsName")

  def gender = column[String]("gender")

  def birthday = column[LocalDate]("birthday")

  def browserUsed = column[String]("browserUsed")

  def creationDate = column[LocalDate]("creationDate")

  def email = column[List[String]]("email")

  def speaks = column[List[String]]("speaks")

  def locationIP = column[String]("locationIP")

  def cityFk = foreignKey("cityFk", cityId, CityDb.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def cityId = column[Long]("cityId")
}

object PersonDb {
  type PersonRecord = (Long, Long, String, String, String, LocalDate, String, LocalDate, List[String], List[String], String)
  val table = TableQuery[PersonDb]
}
