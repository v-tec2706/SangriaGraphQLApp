package benchmark.api

import sangria.schema.{Argument, IntType, LongType, StringType}

object Arguments {
  val Id: Argument[Long] = Argument("id", LongType)
  val Country: Argument[String] = Argument("country", StringType)
  val Year: Argument[Int] = Argument("year", IntType)
}
