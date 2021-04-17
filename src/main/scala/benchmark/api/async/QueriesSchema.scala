package benchmark.api.async

import benchmark.api.Arguments.Id
import benchmark.resolver.MainResolver
import sangria.schema.{Field, FutureValue, ObjectType, Schema, fields}

object QueriesSchema {
  val query: ObjectType[MainResolver, Unit] = ObjectType(
    "Query",
    fields[MainResolver, Unit](
      Field(
        "personAsync",
        AsyncEntities.Person,
        arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id).toLong))
      ),
      Field(
        "personWithArgsAsync",
        AsyncEntities.PersonWithArgs,
        arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id).toLong))
      )
    )
  )

  val asyncSchema: Schema[MainResolver, Unit] = Schema(query)
}
