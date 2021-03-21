package benchmark.api

import sangria.ast.StringValue
import sangria.schema.ScalarType
import sangria.validation.Violation

import java.time.LocalDate
import scala.util.Try

object CustomTypesSchema {

  case object DateTimeCoerceViolation extends Violation {
    override def errorMessage: String = "Error during parsing DateTime"
  }

  implicit val GQLDate: ScalarType[LocalDate] = ScalarType[LocalDate](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _, _, _) => Try(LocalDate.parse(dt)).toEither.fold(_ => Left(DateTimeCoerceViolation), Right(_))
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: String => Try(LocalDate.parse(s)).toEither.fold(_ => Left(DateTimeCoerceViolation), Right(_))
      case _ => Left(DateTimeCoerceViolation)
    }
  )
}
