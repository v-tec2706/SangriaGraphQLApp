package analysis

import sangria.ast.Document
import sangria.macros.LiteralGraphQLStringContext

object Queries {
  val baseQuery: Document =
    graphql"""
      {
        human(ident: 1001) {
          id
          name
          appearsIn
          homePlanet
          friends {
            id
            name
            appearsIn
            homePlanet
            friends {
              id
              name
              appearsIn
              homePlanet
            }
          }
        }
    }
    """

  val baseQuerySimplified: Document =
    graphql"""
      {
        chunk1: human(ident: 1001) {
          id
          name
          appearsIn
          homePlanet
        }
        chunk2: human(ident: 1001) {
          friends {
            id
            name
            appearsIn
            homePlanet
          }
        }
        chunk3: human(ident: 1001) {
          friends {
            friends {
              id
              name
              appearsIn
              homePlanet
            }
          }
        }
     }
    """
}
