package benchmark

import benchmark.BenchmarkQueries.Strategies.Strategy

object BenchmarkQueries {

  val q0: Strategy => Query = strategy =>
    Query(
      "q0",
      s"""query q0 {
      person$strategy(id: 4) {
        firstName
        lastName
      }
    }
  """
    )
  /*
        Query: Q1 - simple navigational query, fetch nested relation with depth = 3, requires concatenation of nested
        entities
        Test: check ability of the resolver to optimize evaluation of sub-queries
   */
  val q1: Strategy => Query = strategy =>
    Query(
      "q1",
      s"""
      query q1 {
        person$strategy(id: 4) {
          firstName
          lastName
          city {
            name
            country {
              name
            }
          }
          university {
             name
             city {
               name
             }
           }
           knows {
               firstName
               lastName
               city {
                 name
                 country {
                   name
                 }
               }
               university {
                  name
                  city {
                    name
                  }
                }
               knows {
                   firstName
                   lastName
                   city {
                     name
                     country {
                       name
                     }
                   }
                   university {
                      name
                      city {
                        name
                     }
                  }
               }
            }
         }
      }
      """
    )
  /*
      Query: Q2 - modified version of Q1 that have additional parameters which specify search condition
      Test: check ability of optimization when search condition is contained in nested field
   */
  val q2: Strategy => Query = strategy =>
    Query(
      "q2",
      s"""
      query q2 {
        personWithArgs$strategy(id: 1) {
          firstName
          lastName
          knows(year: 2000, country: "country-4") {
           id
          }
         }
      }
     """
    )
  /*
    Query: Q3 - query with multi element collections as subfields, elements in collections are rather unique, and only
    small subset of fields is selected
    Test: check ability to optimize (chunk/batch) fetching collections with big size, and postpone projections to the end of
    execution
   */
  val q3: Strategy => Query = strategy =>
    Query(
      "q3",
      s"""
      query q3 {
        person$strategy(id: 20) {
           knows {
             id
             messages {
               id
               content
             }
           }
        }
      }
      """
    )
  /*
  Query: Q4 - "messages of friends of friends" - query with multi element collections as subfields, collections will contain numerous duplicates (common friends)
  Test: check ability to reuse data which was already fetched
   */
  val q4: Strategy => Query = strategy =>
    Query(
      "q4",
      s"""
      query q4 {
        person$strategy(id: 20) {
           knows {
             knows {
               id
               messages {
                 id
                 content
               }
             }
           }
         }
      }
      """
    )
  /*
  Query: Q5 - duplicated sub queries - it's a common case when requests for data are generated automatically (e.q. from UI components)
  Test: check ability to detect and eliminate duplicates
   */
  val q5: Strategy => Query = strategy =>
    Query(
      "q5",
      s"""
      query q5 {
        person$strategy(id: 20) {
          firstName
          knows {
            messages {
              id
            }
          }
          knows {
            messages {
              id
            }
          }
          knows {
            messages {
              id
            }
          }
          knows {
            messages {
              id
            }
          }
        }
      }
      """
    )
  /*
Query: Q6 - simple query without nested fields
Test: check overhead of the resolver
   */
  val q6: Strategy => Query = strategy =>
    Query(
      "q6",
      s"""
      query q6 {
        person$strategy(id: 20) {
          firstName
          lastName
          gender
          birthday
          browserUsed
          creationDate
          locationIP
        }
      }
      """
    )
  /*
Query: Q7 - branching query, different queries are used to fetch the same relation, number of fields
queried on each level differs between sub queries
Test: check added calculation time in case where only a small number of fields queried multiple times on
 the same node
   */
  val q7: Strategy => Query = strategy =>
    Query(
      "q7",
      s"""
      query q7 {
       q7_1:  person$strategy(id: 1) {
           firstName
           university {
             id
             city {
               id
               country {
                 id
                 continent {
                   id
                 }
               }
             }
          }
       }
       q7_2:  person$strategy(id: 1) {
           firstName
           lastName
           university {
             id
             name
             city {
               id
               name
               country {
                 id
                 name
                 continent {
                   id
                   name
                 }
               }
             }
          }
       }
        q7_3:  person$strategy(id: 1) {
           firstName
           lastName
           gender
           university {
             id
             name
             url
             city {
               id
               name
               url
               country {
                 id
                 name
                 url
                 continent {
                   id
                   name
                   url
                 }
               }
             }
          }
       }
      }
      """
    )
  /*
Query: Q8 - extreme blowup
Test: check ability to resolve extremely expensive query (expected result or timout)
   */
  val q8: Strategy => Query = strategy =>
    Query(
      "q8",
      s"""
      query q4 {
        person$strategy(id: 1) {
           knows {
             id
             knows {
               id
               knows {
                 id
                 knows {
                   id
                   messages {
                     id
                   }
                 }
               }
             }
           }
        }
      }
      """
    )
  val all: Strategy => List[Query] = strategy => List(q1, q2, q3, q4, q4, q6, q7, q8).map(_.apply(strategy))

  case class Query(name: String, body: String)

  object Strategies extends Enumeration {
    type Strategy = Value
    val Async, Batched, Cached, BatchedCached = Value
  }

}
