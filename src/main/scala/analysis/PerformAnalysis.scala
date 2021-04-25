package analysis

import benchmark.BenchmarkQueries.Strategies
import benchmark.BenchmarkQueries.Strategies.Strategy
import benchmark.Execution.{ex, stop}
import benchmark.ExecutorProvider
import benchmark.HttpClient.handleResponse
import benchmark.api.async.QueriesSchema.asyncSchema
import sangria.ast.Field
import sangria.parser.QueryParser

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object PerformAnalysis extends App {

  lazy val q: Strategy => String =
    strategy =>
      s"""
      query q1 {
        person$strategy(id: 4) {
          id
          firstName
          lastName
          gender
          birthday
          browserUsed
          city {
            name
          }
          university {
             name
          }
        }
      }
  """
  val analyzer = new QueryAnalyzer
  val queryTypes = analyzer.getQueryTypes(asyncSchema)
  QueryParser
    .parse(q(s))
    .map(analyzer.extractValues)
    .map(_.head.head.asInstanceOf[Field])
    .map(analyzer.splitQuery(_, queryTypes))
    .map(_.map(x => (x._1.renderPretty, x._2)))
    //    .map(_.map(x => s"{ ${assignStrategy(x._1, x._2)} }"))
    .map(_.map { case (str, value) => runQuery(str) })
  //    .map(
  //      _.map(
  //        sendGraphQLRequest("8081", _)
  //          .map(HttpClient.handleResponse(_, "q1", "async"))
  //      ).map(_.map(println))
  //    )
  val s = Strategies.Async

  def assignStrategy(query: String, labels: Set[PerformAnalysis.QueryTypes.QueryType]): String = query

  def runQuery(query: String) = {
    val executor = ExecutorProvider.provide(s)
    val beforeUsedMem: Long = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
    val res = executor
      .parseQuery(query)
      .map(executor.executeQuery)
      .map(_.map(handleResponse(_, " ", s.toString)))

    res.toOption.map(_.onComplete {
      case Success(value) => {
        println(value)
        //          val afterUsedMem = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
        //          saveToFile(value, queryToUse.name, strategyToUse.toString);
        //          saveToFile(s"Memory used: ${(afterUsedMem - beforeUsedMem) / 1000000}\n", queryToUse.name, strategyToUse.toString);
        stop()
      };
      case Failure(exception) => println(s"Error occurred: $exception"); stop()
    })
  }

  object QueryTypes extends Enumeration {
    type QueryType = Value
    val Scalar, Object, Collection, Unknown = Value
  }

}
