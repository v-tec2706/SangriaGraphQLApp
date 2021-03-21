package benchmark.entities

import java.time.LocalDate

case class Forum(
                  id: Long,
                  moderatorId: Long,
                  title: String,
                  creationDate: LocalDate,
                )
