package benchmark.federation

import akka.http.scaladsl.Http
import benchmark.BenchmarkQueries.Strategies
import benchmark.BenchmarkQueries.Strategies.Strategy
import benchmark.Utils.{resolveExecutor, resolveSchema, resolveStrategy}
import benchmark.api.async.AsyncEntities.personResolver
import benchmark.resolver.MainResolver
import benchmark.{FederatedExecutor, Server, api}
import io.circe._
import sangria.execution.deferred.DeferredResolver
import sangria.schema.Schema

import scala.concurrent.Future

case class FederationServer(
                             strategy: Strategy,
                             port: Int,
                             schema: Schema[MainResolver, Unit],
                             deferredResolver: Option[DeferredResolver[MainResolver]] = None
                           ) {

  private val (federatedSchema, um) = sangria.federation.Federation.federate[MainResolver, Unit, Json](
    schema,
    sangria.marshalling.circe.CirceInputUnmarshaller,
    personResolver(MainResolver.build)
  )

  private val execution: FederatedExecutor[MainResolver] =
    FederatedExecutor[MainResolver](MainResolver.build, federatedSchema, deferredResolver, strategy)(um)

  def start: Future[Http.ServerBinding] = {
    println(s"Serving schema: \n ${federatedSchema.renderPretty}")
    Server(strategy, port, execution).start
  }
}

object GraphQL extends App {

  val arguments = args.toList match {
    case strategy :: port :: Nil => Some(resolveStrategy(strategy), port.toInt)
    case _ => None
  }

  arguments match {
    case Some(value) =>
      val (strategy, port) = value; FederationServer(strategy, port, resolveSchema(strategy), resolveExecutor(strategy)).start
    case None => runAll
  }

  def runAll = {
    FederationServer(Strategies.Async, 8081, api.async.QueriesSchema.asyncSchema).start
    FederationServer(
      Strategies.Batched,
      8082,
      api.batch.QueriesSchema.batchedSchema,
      Some(api.batch.QueriesSchema.batchedResolvers)
    ).start
    FederationServer(
      Strategies.Cached,
      8083,
      api.cache.QueriesSchema.cachedSchema,
      Some(api.cache.QueriesSchema.cachedResolvers)
    ).start
    FederationServer(
      Strategies.BatchedCached,
      8084,
      api.batchcache.QueriesSchema.batchedCachedSchema,
      Some(api.batchcache.QueriesSchema.batchedCachedResolvers)
    ).start
  }
}
