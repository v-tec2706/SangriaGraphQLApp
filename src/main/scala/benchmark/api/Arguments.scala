package benchmark.api

import sangria.schema.{Argument, IntType, StringType}

object Arguments {
  val Id: Argument[Int] = Argument("id", IntType)
  val Country: Argument[String] = Argument("country", StringType)
  val Year: Argument[Int] = Argument("year", IntType)
}
