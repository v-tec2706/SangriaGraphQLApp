package analysis

import benchmark.BenchmarkQueries.Strategies
import benchmark.HttpClient.sendGraphQLRequest
import benchmark.api.async.QueriesSchema.asyncSchema
import benchmark.{BenchmarkQueries, HttpClient}
import sangria.ast.Field
import sangria.parser.QueryParser

import scala.concurrent.ExecutionContext.Implicits.global

object PerformAnalysis extends App {

  val analyzer = new QueryAnalyzer
  val queryTypes = analyzer.getQueryTypes(asyncSchema)

  QueryParser
    .parse(BenchmarkQueries.q1(Strategies.Async))
    .map(analyzer.extractValues)
    .map(_.head.head.asInstanceOf[Field])
    .map(analyzer.splitQuery(_, queryTypes))
    .map(_.map(x => (x._1.renderPretty, x._2)))
    .map(_.map(x => s"{ ${assignStrategy(x._1, x._2)} }"))
    .map(
      _.map(
        sendGraphQLRequest("8081", _)
          .map(HttpClient.handleResponse(_, "q1", "async"))
      ).map(_.map(println))
    )

  def assignStrategy(query: String, labels: Set[PerformAnalysis.QueryTypes.QueryType]): String = query

  object QueryTypes extends Enumeration {
    type QueryType = Value
    val Scalar, Object, Collection, Unknown = Value
  }

}
