package analysis

import analysis.QueryAnalysis.TypeExtractor.{complexType, simpleType}
import benchmark.BenchmarkQueries
import benchmark.BenchmarkQueries.Strategies
import benchmark.api.QueriesSchema.benchmarkQuerySchema
import model.SchemaDefinition
import sangria.ast.{Document, Field, Selection}
import sangria.parser.QueryParser
import sangria.schema.{ListType, ObjectType, OptionType, OutputType, ScalarType, Schema}

import scala.annotation.tailrec

object QueryAnalysis extends App {

  val schemaDefinition = SchemaDefinition.StarWarsSchema
  val queryTypes = getQueryTypes(benchmarkQuerySchema)

  QueryParser.parse(BenchmarkQueries.q1(Strategies.Async))
    .map(extractValues)
    .map(_.head.head.asInstanceOf[Field])
    .map(splitQuery(_, queryTypes))
    .map(_.map(_.renderPretty))
    .map(_.map(println))

  def getQueryTypes[Ctx, Val](schema: Schema[Ctx, Val]): Map[String, OutputType[_]] = schema.query.fields.map(x => (x.name, x.fieldType)).toMap

  def extractValues(document: Document): Iterable[Vector[Selection]] = {
    document.operations.values.map(_.selections)
  }

  @tailrec
  def resolveName(field: Field, outputType: OutputType[_], buff: String = ""): String = outputType match {
    case ObjectType(name, _, _, _, _, _, _) => field.name ++ buff ++ "_[object]_" ++ s"{$name}"
    case ScalarType(name, _, _, _, _, _, _, _, _) => field.name ++ buff ++ "_" ++ s"{$name}"
    case OptionType(ofType) => resolveName(field, ofType, "_[option]")
    case ListType(ofType) => resolveName(field, ofType, "_[list]")
  }

  def filterType(field: OutputType[_], f: OutputType[_] => Boolean): Option[OutputType[_]] = Some(field).filter(f)

  @tailrec
  def extractSubfields(fields: OutputType[_], selections: List[String]): Map[String, OutputType[_]] = fields match {
    case a@ObjectType(_, _, _, _, _, _, _) => a.fields.map(x => (x.name, x.fieldType)).filter(x => selections contains x._1).toMap
    case ListType(ofType) => extractSubfields(ofType, selections)
    case _ => Map.empty
  }

  def splitQuery[Ctx, Val](field: Field, types: Map[String, OutputType[_]]): List[Selection] = {
    val rootType = types.get(field.name)
    val innerTypes = rootType.map(extractSubfields(_, field.selections.map { case Field(_, name, _, _, _, _, _, _) => name }.toList)).getOrElse(Map.empty)
    val simpleSubfields = innerTypes.filter(x => simpleType(x._2))
    val complexSubFields = innerTypes.filter(x => complexType(x._2))
    val filterSelection = (subfields: Map[String, OutputType[_]]) => field.selections.filter { case Field(_, name, _, _, _, _, _, _) => subfields.keys.toList contains name }
    val simpleSelection = filterSelection(simpleSubfields)
    val complexSelection = filterSelection(complexSubFields)
    val simpleSubQuery = if (simpleSelection.isEmpty) None else Some(field.copy(selections = simpleSelection.toVector))
    val complexSubQuery = complexSelection
      .map { case field@Field(_, _, _, _, _, _, _, _) => splitQuery(field, innerTypes) }
      .flatMap(x => {
        x.map(y => Some(field.copy(selections = Vector(y))))
      }).toList
    (simpleSubQuery :: complexSubQuery).collect { case Some(value) => value }
  }

  object TypeExtractor {
    val simpleType: OutputType[_] => Boolean = {
      case ListType(_) => false
      case ObjectType(_, _, _, _, _, _, _) => false
      case _ => true
    }

    val complexType: OutputType[_] => Boolean = (x: OutputType[_]) => !simpleType(x)
  }

}
