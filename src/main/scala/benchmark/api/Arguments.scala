package benchmark.api

import sangria.schema.{Argument, IntType, LongType, StringType}

object Arguments {
  val Id: Argument[Long] = Argument("id", LongType)
  val PersonName: Argument[String] = Argument("personName", StringType)
  val InstitutionLocation: Argument[String] = Argument("institutionLocation", StringType)
  val Limit: Argument[Int] = Argument("limit", IntType)
}
