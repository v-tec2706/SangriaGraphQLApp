package database

import app.MyExecutionContext.ex
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.Rep

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait Repository {
  val database: Database

  def insert[S, A <: Table[S]](table: TableQuery[A], items: Seq[S]): DBIOAction[Unit, NoStream, Effect.Schema with Effect.Write] = {
    DBIO.seq(table.schema.createIfNotExists, table ++= items)
  }

  def getByProperty[B, A <: Table[B]](tableQuery: TableQuery[A])(f: A => Rep[Boolean]): Future[Seq[B]] = {
    database.run(tableQuery.filter(f).result)
  }

  def getByProperty2[B, A <: Table[B]](tableQuery: TableQuery[A])(f: A => Rep[Boolean]): DBIO[Seq[B]] = {
    tableQuery.filter(f).result
  }

  def getAll[B, A <: Table[B]](tableQuery: TableQuery[A]): Future[Seq[B]] = {
    database.run(tableQuery.result)
  }

  def mapResults[A, B](result: Future[Seq[A]])(f: A => B): Future[Seq[B]] = result.map(_.map(f))

  def mapResults2[A, B](result: DBIO[Seq[A]])(f: A => B): DBIO[Seq[B]] = result.map(_.map(f))

  def handleResults[A](result: Future[Seq[A]]): Unit = result onComplete {
    case Failure(exception) => s"EXEC ERROR: $exception"
    case Success(value) => value.foreach(println)
  }
}

object Repository {

  lazy val database: Database = Database.forConfig(configPath)
  val configPath = "sangria"
}
