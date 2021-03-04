package database

import slick.jdbc.JdbcBackend.Database

class RepositoryLayer(database: Database) {
  val characterRepository: CharacterRepository = CharacterRepository(database)
}
