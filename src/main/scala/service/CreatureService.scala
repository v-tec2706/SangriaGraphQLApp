package service

import app.MyExecutionContext.ex
import database.{CharacterRepository, FriendsRepository, Repository}
import model.CharacterEntity
import service.CreatureService.inc
import slick.dbio.Effect.All
import slick.dbio.{DBIO, DBIOAction, NoStream}

import scala.concurrent.Future

class CreatureService(characterRepository: CharacterRepository, friendsRepository: FriendsRepository) {

  def getCreatures(ids: List[Int]): Future[Seq[CharacterEntity]] = {
    val req = ids.map(id => getCreature2(id))
    val z: DBIOAction[List[CharacterEntity], NoStream, All] = DBIO.sequence(req)
    Repository.database.run(z)
  }

  def getCreature2(id: Int): DBIOAction[CharacterEntity, NoStream, All] = {
    inc()
    val characterData = characterRepository.getCharacterById2(id)
    val friendsData = friendsRepository.getFriendsById2(id)
    for {
      human <- characterData
      friends <- friendsData.map(_.map(_.friendId))
    } yield CharacterEntity(human._1, human._2, friends.toList, human._3, human._4)
  }

  def getCreature(id: Int): Future[CharacterEntity] = {
    val characterData = characterRepository.getCharacterById(id)
    val friendsData = friendsRepository.getFriendsById(id)
    for {
      human <- characterData
      friends <- friendsData.map(_.map(_.friendId))
    } yield CharacterEntity(human._1, human._2, friends.toList, human._3, human._4)
  }

  def getCreaturesIn(ids: List[Int]): Future[List[CharacterEntity]] = {
    val charactersData = characterRepository.getCharacterByIds(ids)
    for {
      friends <- friendsRepository.getFriendsByIds(ids)
      groupped = friends.groupBy(_.id).mapValues(x => x.map(_.friendId)).toMap
      z <- charactersData
    } yield z.map(entity =>
      CharacterEntity(entity.id, entity.name, groupped.getOrElse(entity.id, List.empty).toList, entity.appearsIn, entity.homePlanet)
    )
  }
}

object CreatureService {
  var counter = 0
  def inc(): Unit = synchronized {
    counter = counter + 1
  }
}
