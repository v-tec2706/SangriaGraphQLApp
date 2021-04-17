package database

import model.FriendDB.FriendType
import model.{FriendDB, FriendsEntity}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.Future

case class FriendsRepository(override val database: Database) extends Repository {
  def insert(items: Seq[FriendType]): Future[Unit] = database.run(super.insert(TableQuery[FriendDB], items))

  def getFriendsById(id: Int): Future[Seq[FriendsEntity]] = mapResults_(
    getByProperty[FriendType, FriendDB](TableQuery[FriendDB]) { friendDB: FriendDB => friendDB.id === id }
  )

  def getFriendsById2(id: Int): DBIO[Seq[FriendsEntity]] = mapResults2_(
    getByProperty2[FriendType, FriendDB](TableQuery[FriendDB]) { friendDB: FriendDB => friendDB.id === id }
  )

  def getFriendsByIds(ids: List[Int]): Future[Seq[FriendsEntity]] = mapResults_(
    getByProperty[FriendType, FriendDB](TableQuery[FriendDB]) { friendDB: FriendDB => friendDB.id inSet ids }
  )

  def mapResults_(result: Future[Seq[FriendType]]): Future[Seq[FriendsEntity]] = super.mapResults(result) { case (id, friendId) =>
    FriendsEntity(id, friendId)
  }

  def mapResults2_(result: DBIO[Seq[FriendType]]): DBIO[Seq[FriendsEntity]] = super.mapResults2(result) { case (id, friendId) =>
    FriendsEntity(id, friendId)
  }

}
