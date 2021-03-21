package benchmark.entities

import java.time.LocalDate

case class Message(
                    id: Long,
                    countryId: Long,
                    personId: Long,
                    content: String,
                    length: Int,
                    browserUsed: String,
                    creationDate: LocalDate,
                    locationIP: String
                  )
