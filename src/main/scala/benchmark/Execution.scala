package benchmark

import io.circe.Json
import io.circe.parser.parse
import sangria.execution.Executor
import sangria.execution.deferred.DeferredResolver
import sangria.marshalling.circe._
import sangria.parser.{QueryParser, SyntaxError}
import sangria.schema.Schema
import sangria.slowlog.SlowLog

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class Execution[Ctx](resolver: Ctx, schema: Schema[Ctx, Unit], deferredResolver: Option[DeferredResolver[Ctx]] = None) {

  def graphql(query: String): Future[Json] = executeQuery(query)

  private def executeQuery(query: String): Future[Json] = {
    implicit val ec = Execution.ex
    QueryParser.parse(query) match {
      case Success(queryAst) => Executor
        .execute(schema, queryAst, resolver,
          deferredResolver = deferredResolver.getOrElse(DeferredResolver.empty),
          middleware = SlowLog.apolloTracing :: Nil
        )
      case Failure(error: SyntaxError) => {
        println(s"Syntax error: $error")
        Future {
          Json.obj(
            "syntaxError" -> parse(error.getMessage).toOption.getOrElse(Json.Null),
            "locations" -> Json.arr(Json.obj(
              "line" -> parse(error.originalError.position.line.toString).toOption.getOrElse(Json.Null),
              "column" -> parse(error.originalError.position.column.toString).toOption.getOrElse(Json.Null))))
        }
      }
      case Failure(error) => throw error
    }
  }
}

object Execution {
  lazy val executors = Executors.newFixedThreadPool(5)
  implicit val ex: ExecutionContextExecutor = ExecutionContext.fromExecutor(executors)

  def stop(): Unit = executors.shutdown()
}
