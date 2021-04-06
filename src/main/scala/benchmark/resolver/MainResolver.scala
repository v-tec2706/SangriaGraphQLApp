package benchmark.resolver

import benchmark.repository._

trait Resolver

case class MainResolver(
                         personResolver: PersonResolver,
                         cityResolver: CityResolver,
                         countryResolver: CountryResolver,
                         continentResolver: ContinentResolver,
                         universityResolver: UniversityResolver,
                         messagesResolver: MessageResolver,
                         companyResolver: CompanyResolver
                       )

object MainResolver {
  val build: MainResolver = MainResolver(
    PersonResolver(PersonRepository()),
    CityResolver(CityRepository()),
    CountryResolver(CountryRepository()),
    ContinentResolver(ContinentRepository()),
    UniversityResolver(UniversityRepository()),
    MessageResolver(MessageRepository()),
    CompanyResolver(CompanyRepository())
  )
}
