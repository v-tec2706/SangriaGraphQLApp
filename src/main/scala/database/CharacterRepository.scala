package database

import app.MyExecutionContext._
import model.CharacterDB
import model.CharacterDB.{CharacterType, characters}
import slick.dbio.Effect.All
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.Rep

import scala.concurrent.Future
case class CharacterEntry(id: Int, name: String, appearsIn: String, homePlanet: String)

case class CharacterRepository(override val database: Database) extends Repository {
  def getAll: Future[Seq[CharacterType]] = super.getAll[CharacterType, CharacterDB](characters)

  def insert(items: Seq[CharacterType]): Future[Unit] = database.run(super.insert(characters, items))

  def getCharacterById(id: Int): Future[CharacterType] = {
    characterGetBy({ character: CharacterDB => character.id === id }).map(_.head)(ex)
  }

  def getCharacterById2(id: Int): DBIOAction[CharacterType, NoStream, All] = {
    characterGetBy2({ character: CharacterDB => character.id === id }).map(_.head)(ex)
  }

  def getCharacterByIds(ids: List[Int]): Future[List[CharacterEntry]] = {
    characterGetBy({ character: CharacterDB => character.id inSet ids }).map(_.map { case (i: Int, n: String, m: String, k: String) => CharacterEntry(i, n, m, k) }).map(x => x.toList)
  }

  private def characterGetBy[A]: (CharacterDB => Rep[Boolean]) => Future[Seq[CharacterType]] = {
    getByProperty[CharacterType, CharacterDB](characters)
  }

  private def characterGetBy2[A]: (CharacterDB => Rep[Boolean]) => DBIO[Seq[CharacterType]] = {
    getByProperty2[CharacterType, CharacterDB](characters)
  }
}
