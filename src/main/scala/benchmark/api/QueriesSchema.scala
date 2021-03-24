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
      //      Field("personByAttr", BenchmarkTypesSchema.Person, arguments = PersonName :: Nil,
      //        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(PersonName)))
      //      ),
      //      Field("friendsMessages", BenchmarkTypesSchema.Person, arguments = Id :: Nil,
      //        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      //      ),
      //      Field("friendsOfFriendsMessages", BenchmarkTypesSchema.Person, arguments = Id :: Nil,
      //        resolve = ctx => FutureValue(ctx.ctx.personResolver.getPerson(ctx.arg(Id)))
      //      )
    )
  )

  val benchmarkQuerySchema: Schema[Resolver, Unit] = Schema(query)
}
