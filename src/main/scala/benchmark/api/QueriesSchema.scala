package benchmark.api

import benchmark.api.Arguments._
import benchmark.resolver.Resolver
import sangria.schema.{ObjectType, _}

object QueriesSchema {
  val query: ObjectType[Resolver, Unit] = ObjectType(
    "Query", fields[Resolver, Unit](
      Field("person", BenchmarkTypesSchema.Person, arguments = Id :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      ),
      Field("personByParam", BenchmarkTypesSchema.Person, arguments = PersonName :: InstitutionLocation :: Limit :: Nil,
        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      )
    )
  )

  val benchmarkQuerySchema: Schema[Resolver, Unit] = Schema(query)
}
