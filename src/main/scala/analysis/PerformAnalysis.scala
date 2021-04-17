package analysis

import benchmark.BenchmarkQueries
import benchmark.BenchmarkQueries.Strategies
import benchmark.api.async.QueriesSchema.asyncSchema
import model.SchemaDefinition
import sangria.ast.Field
import sangria.parser.QueryParser

object PerformAnalysis extends App {

  val schemaDefinition = SchemaDefinition.StarWarsSchema
  val analyzer = new QueryAnalyzer
  val queryTypes = analyzer.getQueryTypes(asyncSchema)

  QueryParser
    .parse(BenchmarkQueries.q1(Strategies.Async))
    .map(analyzer.extractValues)
    .map(_.head.head.asInstanceOf[Field])
    .map(analyzer.splitQuery(_, queryTypes))
    .map(_.map(x => (x._1.renderPretty, x._2)))
    .map(_.map(x => println(s"${x._2.mkString("[", ",", "]")}\n${x._1}")))

  object QueryTypes extends Enumeration {
    type QueryType = Value
    val Scalar, Object, Collection, Unknown = Value
  }

}
