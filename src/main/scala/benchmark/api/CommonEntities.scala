package benchmark.api

import benchmark.entities.Continent
import benchmark.resolver.MainResolver
import sangria.schema.{Field, IntType, ObjectType, StringType, fields}

object CommonEntities {
  lazy val Continent: ObjectType[MainResolver, Continent] = ObjectType(
    "Continent",
    () => fields[MainResolver, Continent](
      Field("id", IntType, resolve = _.value.id.intValue()),
      Field("name", StringType, resolve = _.value.name),
      Field("url", StringType, resolve = _.value.url),
    )
  )
}
