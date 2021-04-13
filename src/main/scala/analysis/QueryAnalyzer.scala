package analysis

import sangria.ast.{Document, Field, Selection}
import sangria.schema.{ListType, ObjectType, OptionType, OutputType, ScalarType, Schema}

import scala.annotation.tailrec
import scala.collection.immutable

class QueryAnalyzer {

  type TypesMap = Map[String, OutputType[_]]

  val filterSelections: (Field, TypesMap) => immutable.Iterable[Field] = (field: Field, subfields: TypesMap) => field
    .selections
    .groupBy { case FieldNameExtractor(name) => name }
    .map { case (_, selections) => mergeFields(selections) }
    .filter { case FieldNameExtractor(name) => subfields.keys.toList contains name }

  private val extractField: (TypesMap, OutputType[_] => Boolean) => TypesMap = (types, cond) =>
    types.filter { case (_, outputType) => cond(outputType) }

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

  def splitQuery[Ctx, Val](field: Field, types: Map[String, OutputType[_]]): List[Selection] = {
    val rootType = types.get(field.name)
    val innerTypes = rootType.map(extractSubfields(_, field.selections.map { case FieldNameExtractor(name) => name }.toList)).getOrElse(Map.empty)

    val simpleSubfields = extractField(innerTypes, TypeExtractor.simpleType)
    val complexSubFields = extractField(innerTypes, TypeExtractor.complexType)
    val simpleSelection = filterSelections(field, simpleSubfields)
    val complexSelection = filterSelections(field, complexSubFields)

    val simpleSubQuery = if (simpleSelection.isEmpty) None else Some(field.copy(selections = simpleSelection.toVector))

    val complexSubQuery = complexSelection
      .map { case field: Field => splitQuery(field, innerTypes) }
      .flatMap(x => {
        x.map(y => Some(field.copy(selections = Vector(y))))
      }).toList

    (simpleSubQuery :: complexSubQuery).collect { case Some(value) => value }
  }

  def mergeFields(fields: Vector[Selection]): Field = fields.collect { case a: Field => a }.reduce((a, b) => a.copy(selections = a.selections ++ b.selections))

  @tailrec
  final def resolveName(field: Field, outputType: OutputType[_], buff: String = ""): String = outputType match {
    case ObjectType(name, _, _, _, _, _, _) => field.name ++ buff ++ "_[object]_" ++ s"{$name}"
    case ScalarType(name, _, _, _, _, _, _, _, _) => field.name ++ buff ++ "_" ++ s"{$name}"
    case OptionType(ofType) => resolveName(field, ofType, "_[option]")
    case ListType(ofType) => resolveName(field, ofType, "_[list]")
  }

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
