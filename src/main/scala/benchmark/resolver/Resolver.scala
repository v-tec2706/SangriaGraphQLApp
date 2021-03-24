package benchmark.resolver

import benchmark.repository._

case class Resolver(
                     personResolver: PersonResolver,
                     cityResolver: CityResolver,
                     countryResolver: CountryResolver,
                     continentResolver: ContinentResolver,
                     universityResolver: UniversityResolver,
                     messagesResolver: MessageResolver
                   )

object Resolver {
  val build: Resolver = Resolver(
    PersonResolver(PersonRepository()),
    CityResolver(CityRepository()),
    CountryResolver(CountryRepository()),
    ContinentResolver(ContinentRepository()),
    UniversityResolver(UniversityRepository()),
    MessageResolver(MessageRepository())
  )
}
