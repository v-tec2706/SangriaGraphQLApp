package benchmark.data.model

import benchmark.data.model.Forum.ForumRecord
import slick.jdbc.H2Profile.api._

import java.time.LocalDate

class Forum(tag: Tag) extends Table[ForumRecord](tag, "Forum") {
  override def * = (id, moderatorId, title, creationDate)

  def id = column[Long]("id", O.PrimaryKey)

  def title = column[String]("title")

  def creationDate = column[LocalDate]("creationDate")

  def moderatorFk = foreignKey("moderatorId", moderatorId, Person.table)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def moderatorId = column[Long]("moderatorId")
}

object Forum {
  type ForumRecord = (Long, Long, String, LocalDate)
  val table = TableQuery[Forum]
}

