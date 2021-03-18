package benchmark.data.model

import benchmark.data.model.Company.CompanyRecord
import slick.jdbc.H2Profile.api._

class Company(tag: Tag) extends Table[CompanyRecord](tag, "Company") {
  override def * = (id, countryId, name, url)

  def id = column[Long]("id", O.PrimaryKey)

  def name = column[String]("name")

  def url = column[String]("url")

  def countryFk = foreignKey("countryFk", countryId, Country.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def countryId = column[Long]("countryId")
}

object Company {
  type CompanyRecord = (Long, Long, String, String)
  val table = TableQuery[Company]
}


