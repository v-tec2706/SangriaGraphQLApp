package benchmark.repository

import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

trait Repository[R, B <: Table[R], E] {
  def table: TableQuery[B]

  def entityMapping: R => E

  def entity(dbRecord: R): E = entityMapping(dbRecord)

  def get(condition: B => Rep[Boolean]): DBIO[Seq[R]] = table.filter(condition).result
}

object Repository {
  lazy val database: Database = Database.forConfig(configPath)
  val configPath = "sangria"

  def runManySeq[A](seq: Seq[DBIOAction[A, NoStream, Effect.All]]): Future[Seq[A]] = Repository.database.run(DBIO.sequence(seq))
}
