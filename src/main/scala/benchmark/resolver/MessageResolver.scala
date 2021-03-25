package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Message
import benchmark.repository.{MessageRepository, Repository}
import sangria.execution.deferred.{Fetcher, FetcherCache, FetcherConfig, HasId}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import scala.concurrent.Future

case class MessageResolver(messagesRepository: MessageRepository) extends Resolver {
  def getMessage(id: Long): Future[Message] = Repository.database.run(messagesRepository.getMessage(id).map(_.head))

  def getMessages(ids: List[Long]): Future[Seq[Message]] = Repository.database.run(messagesRepository.getMessages(ids))

  def getMessageAsync(ids: List[Long]): Future[List[Message]] = Repository.database.run(DBIO.sequence(ids.map(id => messagesRepository.getMessage(id).map(_.head))))

  def getBySender(id: Long): Future[Seq[Long]] = Repository.database.run(messagesRepository.getBySender(id))
}

object MessageResolver {
  implicit val hasId: HasId[Message, Long] = HasId[Message, Long](_.id)
  val batchedMessageResolver: Fetcher[MainResolver, Message, Message, Long] = Fetcher((ctx: MainResolver, ids: Seq[Long]) => ctx.messagesResolver.getMessages(ids.toList))
  val cachedMessageResolver: Fetcher[MainResolver, Message, Message, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => {
      val seq: Seq[DBIOAction[Message, NoStream, Effect.All]] = ids.map(id => ctx.messagesResolver.messagesRepository.getMessage(id).map(_.head))
      val dbioSeq: DBIOAction[Seq[Message], NoStream, Effect.All] = DBIO.sequence(seq)
      Repository.database.run(dbioSeq)
    })
  val batchedCachedMessageResolver: Fetcher[MainResolver, Message, Message, Long] = Fetcher(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.messagesResolver.getMessages(ids.toList)
  )
}