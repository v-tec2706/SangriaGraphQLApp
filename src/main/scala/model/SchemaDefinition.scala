package model

import sangria.schema._
import service.CreatureDeferredResolver.{batchedCreatures, cachedBatchedCreatures, cachedCreatures}
import service.CreatureService

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {

  lazy val Human: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "Human",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        resolve = _.value.name),
      Field("friends", ListType(Human),
        resolve = ctx => ctx.ctx.getCreatures(ctx.value.friends)),
      Field("appearsIn", StringType,
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
        resolve = _.value.homePlanet)
    ))

  lazy val HumanBatched: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "HumanDeferred",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        resolve = _.value.name),
      Field("friends", ListType(HumanBatched),
        resolve = ctx => batchedCreatures.deferSeqOpt(ctx.value.friends.toSeq)),
      Field("appearsIn", StringType,
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
        resolve = _.value.homePlanet)
    ))

  lazy val HumanCachedBatched: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "HumanCached",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        resolve = _.value.name),
      Field("friends", ListType(HumanCachedBatched),
        resolve = ctx => cachedBatchedCreatures.deferSeqOpt(ctx.value.friends.toSeq)),
      Field("appearsIn", StringType,
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
        resolve = _.value.homePlanet)
    ))

  lazy val HumanCached: ObjectType[CreatureService, CharacterEntity] = ObjectType(
    "HumanCached",
    () => fields[CreatureService, CharacterEntity](
      Field("id", IntType,
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        resolve = _.value.name),
      Field("friends", ListType(HumanCached),
        resolve = ctx => cachedCreatures.deferSeqOpt(ctx.value.friends.toSeq)),
      Field("appearsIn", StringType,
        resolve = _.value.appearsIn),
      Field("homePlanet", StringType,
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
        resolve = ctx => batchedCreatures.defer(ctx.arg(Id))),
      Field("humanCachedBatched", HumanCachedBatched,
        arguments = Id :: Nil,
        resolve = ctx => cachedBatchedCreatures.defer(ctx.arg(Id))),
      Field("humanCached", HumanCached,
        arguments = Id :: Nil,
        resolve = ctx => cachedCreatures.defer(ctx.arg(Id))))
  )
  val StarWarsSchema = Schema(Query)
}
