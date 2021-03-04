package database

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

object InitializeMockData extends App {
  val characterRepo = CharacterRepository(Repository.database)
  val friendsRepo = FriendsRepository(Repository.database)

  val z = characterRepo.insert((1000 to 2000).map(id => (id, s"name-$id", s"appearsIn-$id", s"homePlanet-$id")))
  val k = friendsRepo.insert(
    (1000 to 2000).flatMap(id => (1 to 20).map(_ => (id, Random.between(1000, 2000))))
  )
  Await.ready(z, Duration.Inf)
  Await.ready(k, Duration.Inf)
}
