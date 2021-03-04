package model

import database.CharacterEntry
import sangria.schema._
import service.CreatureDeferredResolver.{batchedCreatures, cachedBatchedCreatures, cachedCreatures}
import service.CreatureService

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {

  lazy val Human: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "Human",
    "A humanoid creature in the Star Wars universe.",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        Some("The id of the human."),
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        Some("The name of the human."),
        resolve = _.value.name),
      Field("friends", ListType(Human),
        Some("The friends of the human, or an empty list if they have none."),
        resolve = ctx => ctx.ctx.getCreatures(ctx.value.friends)),
      Field("appearsIn", StringType,
        Some("Which movies they appear in."),
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
        Some("The home planet of the human, or null if unknown."),
        resolve = _.value.homePlanet)
    ))

  lazy val HumanBatched: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "HumanDeferred",
    "A humanoid creature in the Star Wars universe.",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        Some("The id of the human."),
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        Some("The name of the human."),
        resolve = _.value.name),
      Field("friends", ListType(HumanBatched),
        Some("The friends of the human, or an empty list if they have none."),
        resolve = ctx => batchedCreatures.deferSeqOpt(ctx.value.friends.toSeq)),
      Field("appearsIn", StringType,
        Some("Which movies they appear in."),
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
        Some("The home planet of the human, or null if unknown."),
        resolve = _.value.homePlanet)
    ))

  lazy val HumanCachedBatched: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "HumanCached",
    "A humanoid creature in the Star Wars universe.",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        Some("The id of the human."),
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        Some("The name of the human."),
        resolve = _.value.name),
      Field("friends", ListType(HumanCachedBatched),
        Some("The friends of the human, or an empty list if they have none."),
        resolve = ctx => cachedBatchedCreatures.deferSeqOpt(ctx.value.friends.toSeq)),
      Field("appearsIn", StringType,
        Some("Which movies they appear in."),
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
        Some("The home planet of the human, or null if unknown."),
        resolve = _.value.homePlanet)
    ))

  lazy val HumanCached: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "HumanCached",
    "A humanoid creature in the Star Wars universe.",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        Some("The id of the human."),
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        Some("The name of the human."),
        resolve = _.value.name),
      Field("friends", ListType(HumanCached),
        Some("The friends of the human, or an empty list if they have none."),
        resolve = ctx => cachedCreatures.deferSeqOpt(ctx.value.friends.toSeq)),
      Field("appearsIn", StringType,
        Some("Which movies they appear in."),
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
        Some("The home planet of the human, or null if unknown."),
        resolve = _.value.homePlanet)
    ))

  val Id: Argument[Int] = Argument("ident", IntType)

  val Query: ObjectType[CreatureService, Unit] = ObjectType(
    "Query", fields[CreatureService, Unit](
      Field("human", Human,
        arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.getCreature(ctx.arg(Id)))),
      Field("humanDeferred", HumanBatched,
        arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.getCreature(ctx.arg(Id)))),
      Field("humanCachedBatched", HumanCachedBatched,
        arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.getCreature(ctx.arg(Id)))),
      Field("humanCached", HumanCached,
        arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.getCreature(ctx.arg(Id))))
    )
  )
  val StarWarsSchema = Schema(Query)
}
