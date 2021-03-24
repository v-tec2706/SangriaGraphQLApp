package benchmark.resolver

import benchmark.entities.Message
import benchmark.repository.{MessageRepository, Repository}

import scala.concurrent.Future

case class MessageResolver(messagesRepository: MessageRepository) {
  def getBySender(id: Long): Future[Seq[Message]] = {
    Repository.database.run(messagesRepository.getBySender(id))
  }
}
