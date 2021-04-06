package benchmark.api

import benchmark.api.Arguments._
import benchmark.resolver.{MainResolver, PersonResolver}
import sangria.schema.{ObjectType, _}

object QueriesSchema {
  val query: ObjectType[MainResolver, Unit] = ObjectType(
    "Query", fields[MainResolver, Unit](
      Field("person", AsyncEntities.Person, arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      ),
      Field("personWithArgs", AsyncEntities.PersonWithArgs, arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      ),
      Field("personBatched", BatchedEntities.Person, arguments = Id :: Nil,
        resolve = ctx => PersonResolver.batchedPersonResolver.defer(ctx.arg(Id)))
    )
  )

  val benchmarkQuerySchema: Schema[MainResolver, Unit] = Schema(query)
}
