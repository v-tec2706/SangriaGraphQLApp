package analysis

import benchmark.api.QueriesSchema.benchmarkQuerySchema
import model.SchemaDefinition
import sangria.ast.Field
import sangria.parser.QueryParser

object PerformAnalysis extends App {

  val schemaDefinition = SchemaDefinition.StarWarsSchema
  val analyzer = new QueryAnalyzer
  val queryTypes = analyzer.getQueryTypes(benchmarkQuerySchema)

  QueryParser.parse(Queries.z)
    .map(analyzer.extractValues)
    .map(_.head.head.asInstanceOf[Field])
    .map(analyzer.splitQuery(_, queryTypes))
    .map(_.map(_.renderPretty))
    .map(_.map(println))
}
