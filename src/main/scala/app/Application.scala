package app

import database.{CharacterRepository, FriendsRepository, Repository}
import io.circe._
import io.circe.parser._
import model.SchemaDefinition
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.marshalling.circe._
import sangria.parser.{QueryParser, SyntaxError}
import sangria.renderer.SchemaRenderer
import sangria.slowlog.SlowLog
import service.CreatureDeferredResolver.{batchedCreatures, cachedBatchedCreatures, cachedCreatures}
import service.CreatureService

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.{Failure, Success}

class Application {

  lazy val exceptionHandler = ExceptionHandler {
    case (_, error@TooComplexQueryError) => HandledException(error.getMessage)
    case (_, error@MaxQueryDepthReachedError(_)) => HandledException(error.getMessage)
  }

  def graphql(query: String, variables: Option[String], operation: Option[String], tracingEnabled: Boolean = true): Future[Json] = {
    executeQuery(query, variables.map(parse).flatMap(_.toOption), operation, tracingEnabled)
  }

  private def executeQuery(query: String, variables: Option[Json], operation: Option[String], tracing: Boolean): Future[Json] = {
    implicit val z = MyExecutionContext.ex
    val characterRepository = CharacterRepository(Repository.database)
    QueryParser.parse(query) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst) => Executor
        .execute(SchemaDefinition.StarWarsSchema,
          queryAst,
          new CreatureService(characterRepository, FriendsRepository(Repository.database)),
          deferredResolver = DeferredResolver.fetchers(batchedCreatures, cachedBatchedCreatures, cachedCreatures),
          middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil
        )


      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) => Future {
        Json.obj(
          "syntaxError" -> parse(error.getMessage).toOption.getOrElse(Json.Null),
          "locations" -> Json.arr(Json.obj(
            "line" -> parse(error.originalError.position.line.toString).toOption.getOrElse(Json.Null),
            "column" -> parse(error.originalError.position.column.toString).toOption.getOrElse(Json.Null))))
      }
      case Failure(error) => throw error
    }
  }

  def renderSchema: String = SchemaRenderer.renderSchema(SchemaDefinition.StarWarsSchema)

  case object TooComplexQueryError extends Exception("Query is too expensive.")

}

object MyExecutionContext {
  implicit val ex = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
}
