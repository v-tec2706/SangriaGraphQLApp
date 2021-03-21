package benchmark.entities

import java.time.LocalDate

case class Comment(
                    id: Long,
                    countryId: Long,
                    personId: Long,
                    messageId: Long,
                    content: String,
                    length: Int,
                    browserUsed: String,
                    creationDate: LocalDate,
                    locationIP: String
                  )
