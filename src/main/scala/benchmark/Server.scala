package benchmark

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import benchmark.BenchmarkQueries.Strategies
import benchmark.BenchmarkQueries.Strategies.Strategy
import benchmark.SangriaAkkaHttp._
import benchmark.Server.corsHandler
import benchmark.Utils.resolveStrategy
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport.jsonMarshaller
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.circe._

import scala.concurrent.Future
import scala.util.{Failure, Success}

case class Server(strategy: Strategy, port: Int, executor: Execution) {
  implicit val system = ActorSystem("sangria-server")

  import system.dispatcher

  val serverPort = sys.props.get("http.port").fold(port)(_.toInt)
  val interface = "0.0.0.0"

  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { _ =>
      path("graphql") {
        graphQLPlayground ~
          prepareGraphQLRequest {
            case Success(GraphQLRequest(query, _, _)) =>
              val graphQLResponse = executor
                .executeQuery(query)
                .map(OK -> _)
                .recover {
                  case error: QueryAnalysisError => BadRequest -> error.resolveError
                  case error: ErrorWithResolver => InternalServerError -> error.resolveError
                }
              complete(graphQLResponse)
            case Failure(preparationError) => complete(BadRequest, formatError(preparationError))
          }
      }
    } ~
      (get & pathEndOrSingleSlash) {
        redirect("/graphql", PermanentRedirect)
      }

  def start: Future[Http.ServerBinding] = Http().newServerAt(interface, serverPort).bindFlow(corsHandler(route))
}

object Server extends App with CorsSupport {
  implicit val system: ActorSystem = ActorSystem("sangria-server")

  val (strategy: String, port: Int) = args.toList match {
    case List(strategy, port: String) => (strategy, port.toInt)
    case _ => println(
      """Arguments are not correct.
        | Usage: <strategy name> <portNumber>""".stripMargin)
  }

  val strategyToUse = resolveStrategy(args).getOrElse(Strategies.Async)
  val executor: Execution = ExecutorProvider.provide(strategyToUse)

  Server(strategyToUse, port, executor).start
}
