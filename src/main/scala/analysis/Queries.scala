package analysis

import sangria.ast.Document
import sangria.macros.LiteralGraphQLStringContext

object Queries {
  val baseQuery: Document =
    graphql"""
      { human(ident: 1001) {
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
            friends {
              id
              name
              appearsIn
              homePlanet
            }
          }
        }
      }
    }
    """
}
