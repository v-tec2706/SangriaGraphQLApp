package model

import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery

import scala.language.higherKinds

case class CharacterEntity(id: Int, name: String, friends: List[Int], appearsIn: String, homePlanet: String)

class CharacterDB(tag: Tag) extends Table[(Int, String, String, String)](tag, "CHARACTERS") {
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, appearsIn, homePlanet)

  def id = column[Int]("id", O.PrimaryKey) // This is the primary key column

  def name = column[String]("name")

  def appearsIn = column[String]("appearsIn")

  def homePlanet = column[String]("homePlanet")
}

object CharacterDB {
  type CharacterType = (Int, String, String, String)
  val characters: TableQuery[CharacterDB] = TableQuery[CharacterDB]
}



