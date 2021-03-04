object Queries {

  val baseQuery =
    """
    query someQuery {
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

  val humanDeferredQuery =
    """
    query someQuery {
      humanDeferred(ident: 1001) {
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

  val humanCachedQuery =
    """
    query someQuery {
      humanCached(ident: 1001) {
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

  val humanCachedBatchedQuery =
    """
    query someQuery {
      humanCachedBatched(ident: 1001) {
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
}
