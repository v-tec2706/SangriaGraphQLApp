package benchmark

import benchmark.BenchmarkQueries.Strategies.Strategy
import io.circe.Json
import io.circe.parser.parse
import sangria.ast.Document
import sangria.execution.Executor
import sangria.execution.deferred.DeferredResolver
import sangria.marshalling.InputUnmarshaller
import sangria.parser.{QueryParser, SyntaxError}
import sangria.schema.Schema
import sangria.slowlog.SlowLog

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

abstract class Execution(strategy: Strategy) {

  implicit val ec: ExecutionContextExecutor = Execution.ex

  def strategy(s: Strategy): Boolean = strategy == s

  def executeQuery(query: Document): Future[Json]

  def parseQuery(query: String): Either[Json, Document] = {
    QueryParser.parse(query) match {
      case Success(queryAst) => Right(queryAst)
      case Failure(error: SyntaxError) => {
        println(s"Syntax error: $error")
        Left {
          Json.obj(
            "syntaxError" -> parse(error.getMessage).toOption.getOrElse(Json.Null),
            "locations" -> Json.arr(
              Json.obj(
                "line" -> parse(error.originalError.position.line.toString).toOption.getOrElse(Json.Null),
                "column" -> parse(error.originalError.position.column.toString).toOption.getOrElse(Json.Null)
              )
            )
          )
        }
      }
      case Failure(error) => throw error
    }
  }
}

case class StandardExecutor[Ctx](
                                  resolver: Ctx,
                                  schema: Schema[Ctx, Unit],
                                  deferredResolver: Option[DeferredResolver[Ctx]] = None,
                                  strategy: Strategy
                                ) extends Execution(strategy) {
  def executeQuery(query: Document): Future[Json] = {
    import sangria.marshalling.circe._
    Executor.execute(
      schema,
      query,
      resolver,
      deferredResolver = deferredResolver.getOrElse(DeferredResolver.empty),
      middleware = SlowLog.apolloTracing :: Nil
    )
  }
}

case class FederatedExecutor[Ctx](
                                   resolver: Ctx,
                                   schema: Schema[Ctx, Unit],
                                   deferredResolver: Option[DeferredResolver[Ctx]] = None,
                                   strategy: Strategy
                                 )(implicit umIm: InputUnmarshaller[Json])
  extends Execution(strategy) {
  def executeQuery(query: Document): Future[Json] = {
    import sangria.marshalling.circe.CirceResultMarshaller
    Executor.execute(
      schema,
      query,
      resolver,
      deferredResolver = deferredResolver.getOrElse(DeferredResolver.empty),
      middleware = SlowLog.apolloTracing :: Nil
    )
  }
}

object Execution {
  lazy val executors: ExecutorService = Executors.newFixedThreadPool(5)
  implicit val ex: ExecutionContextExecutor = ExecutionContext.fromExecutor(executors)

  def stop(): Unit = executors.shutdown()
}
