package benchmark.entities

import java.time.LocalDate

case class Person(
                   id: Long,
                   cityId: Long,
                   firstName: String,
                   lastName: String,
                   gender: String,
                   birthday: LocalDate,
                   browserUsed: String,
                   creationDate: LocalDate,
                   email: List[String],
                   speaks: List[String],
                   locationIP: String
                 )
