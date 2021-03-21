package benchmark

object BenchmarkQueries {
  val q1: String = // simple navigational query, fetch nested relation with depth = 3
    """
      |query q1 {
      |  person(id: 1) {
      |    firstName
      |    lastName
      |    city {
      |      name
      |      country {
      |        name
      |      }
      |    }
      |    university {
      |       name
      |       city {
      |         name
      |       }
      |     }
      |  }
      |}
      |""".stripMargin

  val q2: String = // modified version of q1 that have additional parameters which specify search condition
    """
      |query q1 {
      |  person(id: 1) {
      |    firstName
      |    lastName
      |    city {
      |      name
      |      country {
      |        name
      |      }
      |    }
      |    university {
      |       name
      |       city {
      |         name
      |       }
      |     }
      |  }
      |}
      |""".stripMargin


}
