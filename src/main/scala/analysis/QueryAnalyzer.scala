package analysis

import analysis.PerformAnalysis.QueryTypes
import analysis.PerformAnalysis.QueryTypes.QueryType
import sangria.ast.{Document, Field, Selection}
import sangria.schema.{ListType, ObjectType, OptionType, OutputType, ScalarType, Schema, Type}

import scala.annotation.tailrec
import scala.collection.immutable

class QueryAnalyzer {

  type TypesMap = Map[String, OutputType[_]]

  val filterSelections: (Field, Map[String, List[AnalyzedField]]) => immutable.Iterable[(Field, QueryType)] = (field, subfields) => field
    .selections
    .groupBy { case FieldNameExtractor(name) => name }
    .map { case (_, selections) => mergeFields(selections) }
    .filter { case FieldNameExtractor(name) => subfields.contains(name) }
    .map(field => (field, subfields.get(field.name).map(_.map(_.label).head).getOrElse(QueryTypes.Unknown)))

  private val extractField: (List[AnalyzedField], OutputType[_] => Boolean) => List[AnalyzedField] = (fields, cond) =>
    fields.filter(a => cond(a.ofType))

  def getQueryTypes[Ctx, Val](schema: Schema[Ctx, Val]): Map[String, OutputType[_]] = schema.query.fields.map(x => (x.name, x.fieldType)).toMap

  def extractValues(document: Document): Iterable[Vector[Selection]] = {
    document.operations.values.map(_.selections)
  }

  def filterType(field: OutputType[_], f: OutputType[_] => Boolean): Option[OutputType[_]] = Some(field).filter(f)

  @tailrec
  final def extractSubfields(fields: OutputType[_], selections: List[String]): Map[String, OutputType[_]] = fields match {
    case a: ObjectType[_, _] => a.fields.map(x => (x.name, x.fieldType)).filter(x => selections contains x._1).toMap
    case ListType(ofType) => extractSubfields(ofType, selections)
    case _ => Map.empty
  }

  def splitQuery[Ctx, Val](field: Field, types: Map[String, OutputType[_]]): List[(Selection, Set[QueryType])] = {
    val rootSelection = types.get(field.name)
    val innerSelection = rootSelection.map(extractSubfields(_, field.selections.map { case FieldNameExtractor(name) => name }.toList)).getOrElse(Map.empty)
    val innerSelectionTypes: List[AnalyzedField] = innerSelection.map { case (name, field) => AnalyzedField(name, field, resolveType(field)) }.toList
    val simpleSubfields = extractField(innerSelectionTypes, TypeExtractor.simpleType)
    val complexSubFields = extractField(innerSelectionTypes, TypeExtractor.complexType)
    val simpleSelection = filterSelections(field, simpleSubfields.groupBy(_.name))
    val complexSelection = filterSelections(field, complexSubFields.groupBy(_.name))

    val simpleSubQuery = if (simpleSelection.isEmpty) None else Some(field.copy(selections = simpleSelection.map(_._1).toVector), simpleSelection.map(_._2).toSet)

    val complexSubQuery: Seq[Some[(Field, Set[QueryType])]] = complexSelection
      .map { case (field, qType) => (splitQuery(field, innerSelection), qType) }
      .flatMap { case (value, queryType) => value.map { case (selection, types) => Some(field.copy(selections = Vector(selection)), types + queryType) } }.toList

    (simpleSubQuery :: complexSubQuery.toList).collect { case Some(value) => value }
  }

  def mergeFields(fields: Vector[Selection]): Field = fields.collect { case a: Field => a }.reduce((a, b) => a.copy(selections = a.selections ++ b.selections))

  @tailrec
  final def resolveName(field: Field, outputType: OutputType[_], buff: String = ""): String = outputType match {
    case ObjectType(name, _, _, _, _, _, _) => field.name ++ buff ++ "_[object]_" ++ s"{$name}"
    case ScalarType(name, _, _, _, _, _, _, _, _) => field.name ++ buff ++ "_" ++ s"{$name}"
    case OptionType(ofType) => resolveName(field, ofType, "_[option]")
    case ListType(ofType) => resolveName(field, ofType, "_[list]")
  }

  def resolveType(input: Type) = input match {
    case _: ListType[_] => QueryTypes.Collection
    case _: ObjectType[_, _] => QueryTypes.Object
    case _ => QueryTypes.Scalar
  }

  case class AnalyzedField(name: String, ofType: OutputType[_], label: QueryType)

  object FieldNameExtractor {
    def unapply(field: Field): Option[String] = Some(field.name)
  }

  object TypeExtractor {
    val simpleType: OutputType[_] => Boolean = {
      case _: ListType[_] => false
      case _: ObjectType[_, _] => false
      case _ => true
    }

    val complexType: OutputType[_] => Boolean = (x: OutputType[_]) => !simpleType(x)
  }

}
