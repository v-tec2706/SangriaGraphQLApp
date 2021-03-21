package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.University
import benchmark.repository.{Repository, UniversityRepository}

import scala.concurrent.Future

case class UniversityResolver(universityRepository: UniversityRepository) {
  def getUniversity(id: Long): Future[University] = Repository.database.run(universityRepository.getUniversity(id)).map(_.head)

  def byStudent(personId: Long): Future[University] = Repository.database.run(universityRepository.studyAt(personId)).map(_.head)
}
