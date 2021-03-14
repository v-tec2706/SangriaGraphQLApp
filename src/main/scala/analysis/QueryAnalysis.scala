package analysis

import analysis.QueryAnalysis.Selections.simpleType
import model.SchemaDefinition
import sangria.ast.{Document, Field, Selection}
import sangria.schema.{ListType, ObjectType, OptionType, OutputType, ScalarType, Schema}

import scala.annotation.tailrec

object QueryAnalysis extends App {

  val schemaDefinition = SchemaDefinition.StarWarsSchema
  val queryTypes = getQueryTypes(schemaDefinition)

  filterTypes2_(extractValues(analysis.Queries.baseQuery).head.head.asInstanceOf[Field], queryTypes).map(x => println(x.renderPretty))
  //  filterTypes(extractValues(analysis.Queries.baseQuery).head.head.asInstanceOf[Field], queryTypes, Selections.listNotSelected).map(x => println(x.renderPretty))

  def getQueryTypes[Ctx, Val](schema: Schema[Ctx, Val]): Map[String, OutputType[_]] = schema.query.fields.map(x => (x.name, x.fieldType)).toMap

  def takeSimple(doc: Document): (Field, Field) = {
    val field = extractValues(doc).head.head.asInstanceOf[Field]
    val simpleSubFields = field.selections.filter { case Field(_, _, _, _, selections, _, _, _) => selections.isEmpty }
    val complexSubFields = field.selections.diff(simpleSubFields)

    (field.copy(selections = simpleSubFields), field.copy(selections = complexSubFields))
  }

  def extractValues(document: Document): Iterable[Vector[Selection]] = {
    document.operations.values.map(_.selections)
  }

  def resolveTypes[Ctx, Val](field: Field, types: Map[String, OutputType[_]]): Selection = {
    val rootType = types.get(field.name)
    val newAlias = rootType.map(resolveName(field, _)).getOrElse(field.name)
    val innerTypes = rootType.map(extractSubfields).getOrElse(Map.empty)
    val selections = field.selections.map { case field@Field(_, _, _, _, _, _, _, _) => resolveTypes(field, innerTypes) }
    field.copy(name = newAlias, selections = selections)
  }

  @tailrec
  def resolveName(field: Field, outputType: OutputType[_], buff: String = ""): String = outputType match {
    case ObjectType(name, _, _, _, _, _, _) => field.name ++ buff ++ "_[object]_" ++ s"{$name}"
    case ScalarType(name, _, _, _, _, _, _, _, _) => field.name ++ buff ++ "_" ++ s"{$name}"
    case OptionType(ofType) => resolveName(field, ofType, "_[option]")
    case ListType(ofType) => resolveName(field, ofType, "_[list]")
  }

  def filterTypes[Ctx, Val](field: Field, types: Map[String, OutputType[_]], selection: OutputType[_] => Boolean): Option[Selection] = {
    val rootType = types.get(field.name)
    val innerTypes = rootType.map(extractSubfields).getOrElse(Map.empty)
    rootType.map(_ => {
      val selections = field.selections.flatMap { case field@Field(_, _, _, _, _, _, _, _) => filterTypes_(field, innerTypes, selection) }
      field.copy(selections = selections)
    })
  }

  def filterTypes_[Ctx, Val](field: Field, types: Map[String, OutputType[_]], selection: OutputType[_] => Boolean): Option[Selection] = {
    val rootType = types.get(field.name)
    val rootTypeFiltered = rootType.map(filterType(_, selection)).filter(_.isDefined)
    val innerTypes = rootType.map(extractSubfields).getOrElse(Map.empty)
    rootTypeFiltered.map(_ => {
      val selections = field.selections.flatMap { case field@Field(_, _, _, _, _, _, _, _) => filterTypes_(field, innerTypes, selection) }
      field.copy(selections = selections)
    })
  }

  def filterType(field: OutputType[_], f: OutputType[_] => Boolean): Option[OutputType[_]] = Some(field).filter(f)

  @tailrec
  def extractSubfields(fields: OutputType[_]): Map[String, OutputType[_]] = fields match {
    case a@ObjectType(_, _, _, _, _, _, _) => a.fields.map(x => (x.name, x.fieldType)).toMap
    case ListType(ofType) => extractSubfields(ofType)
    case _ => Map.empty
  }

  //    def filterTypes2[Ctx, Val](field: Field, types: Map[String, OutputType[_]], selection: OutputType[_] => Boolean): Option[Selection] = {
  //      val rootType = types.get(field.name)
  //      val innerTypes = rootType.map(extractSubfields).getOrElse(Map.empty)
  //      rootType.map(_ => {
  //        val selections: Seq[(Option[Selection], Option[Selection])] = field.selections.map { case field@Field(_, _, _, _, _, _, _, _) => filterTypes2_(field, innerTypes) }
  //        field.copy(selections = selections.flatMap(_._1).toVector)
  //      })
  //    }

  def filterTypes2_[Ctx, Val](field: Field, types: Map[String, OutputType[_]]): List[Selection] = {
    val rootType = types.get(field.name)
    val innerTypes = rootType.map(extractSubfields).getOrElse(Map.empty)
    rootType.map(_ => {
      val simpleSelections: Seq[Selection] = field.selections.flatMap { case field@Field(_, _, _, _, _, _, _, _) => filterTypes_(field, innerTypes, simpleType) }
      val complexSelections = field.selections.map { case field@Field(_, _, _, _, _, _, _, _) => filterTypes2_(field, innerTypes) }
      val simple = field.copy(name = "simple", selections = simpleSelections.toVector)
      val complex = complexSelections.map(x => field.copy(name = "complex", selections = x.toVector))
      simple :: complex.toList
    }).getOrElse(List.empty)
  }

  object Selections {
    val simpleType: OutputType[_] => Boolean = {
      case ListType(_) => false
      case ObjectType(_, _, _, _, _, _, _) => false
      case _ => false
    }

    val complexType: OutputType[_] => Boolean = {
      case ListType(_) => true
      case ObjectType(_, _, _, _, _, _, _) => true
      case _ => true
    }
  }

}
