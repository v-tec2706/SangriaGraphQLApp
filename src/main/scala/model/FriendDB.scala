package model

import model.FriendDB.FriendType
import slick.jdbc.H2Profile.api._

import scala.language.higherKinds

case class FriendsEntity(id: Int, friendId: Int)

class FriendDB(tag: Tag) extends Table[FriendType](tag, "FRIENDS") {
  def * = (id, friendId)

  def id = column[Int]("id")

  def friendId = column[Int]("friendId")
}

object FriendDB {
  type FriendType = (Int, Int)
}
