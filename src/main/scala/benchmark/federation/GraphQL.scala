package benchmark.federation

import benchmark.api.async.AsyncEntities.personResolverF
import benchmark.resolver.MainResolver
import io.circe._

trait GraphQL[F[_]] {

  def query(request: Json): F[Either[Json, Json]]

  def query(
             query: String,
             operationName: Option[String],
             variables: Json
           ): F[Either[Json, Json]]
}

object GraphQL extends App {

  val (schema, um) = sangria.federation.Federation.federate[MainResolver, Unit, Json](
    benchmark.api.async.QueriesSchema.asyncSchema,
    sangria.marshalling.circe.CirceInputUnmarshaller,
    personResolverF(MainResolver.build))

  println(schema.renderPretty)
}
