package benchmark.api

import benchmark.api.Arguments._
import benchmark.resolver.{MainResolver, PersonResolver}
import sangria.schema.{Field, ObjectType, _}

object QueriesSchema {
  val query: ObjectType[MainResolver, Unit] = ObjectType(
    "Query", fields[MainResolver, Unit](
      Field("personAsync", AsyncEntities.Person, arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      ),
      Field("personWithArgsAsync", AsyncEntities.PersonWithArgs, arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      ),
      Field("personBatched", BatchedEntities.Person, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.arg(Id))
      ),
      Field("personWithArgsBatched", BatchedEntities.PersonWithArgs, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.arg(Id))
      ),
      Field("personCached", CachedEntities.Person, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.cachedPersonResolver.defer(ctx.arg(Id))
      ),
      Field("personWithArgsCached", CachedEntities.PersonWithArgs, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.cachedPersonResolver.defer(ctx.arg(Id))
      ),
      Field("personBatchedCached", CachedBatchedEntities.Person, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedCachedPersonResolver.defer(ctx.arg(Id))
      ),
      Field("personWithArgsBatchedCached", CachedBatchedEntities.PersonWithArgs, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedCachedPersonResolver.defer(ctx.arg(Id))
      )
    )
  )

  val benchmarkQuerySchema: Schema[MainResolver, Unit] = Schema(query)
}
