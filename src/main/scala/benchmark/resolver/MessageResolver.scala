package benchmark.resolver

import benchmark.Execution._
import benchmark.entities.Message
import benchmark.repository.Repository.runManySeq
import benchmark.repository.{MessageRepository, Repository}
import sangria.execution.deferred._
import slick.dbio.DBIO

import scala.concurrent.Future

case class MessageResolver(messagesRepository: MessageRepository) extends Resolver {
  def getMessage(id: Long): Future[Message] = Repository.database.run(messagesRepository.getMessage(id).map(_.head))

  def getMessages(ids: Seq[Long]): Future[Seq[Message]] = Repository.database.run(messagesRepository.getMessages(ids.toList))

  def getMessageAsync(ids: List[Long]): Future[List[Message]] =
    Repository.database.run(DBIO.sequence(ids.map(id => messagesRepository.getMessage(id).map(_.head))))

  def getBySender(id: Long): Future[Seq[Long]] = Repository.database.run(messagesRepository.getBySender(id))

  def getMessagesBySenders(ids: Seq[Long]): Future[Seq[Long]] = Repository.database.run(messagesRepository.getBySender(ids))
}

object MessageResolver {
  implicit val hasId: HasId[Message, Long] = HasId[Message, Long](_.id)
  val messageBySender: Relation[Message, Message, Long] = Relation[Message, Long]("bySender", l => Seq(l.personId))
  val batchedMessageResolver: Fetcher[MainResolver, Message, Message, Long] =
    Fetcher.rel(
      (ctx: MainResolver, ids: Seq[Long]) => ctx.messagesResolver.getMessages(ids.toList),
      (ctx: MainResolver, ids: RelationIds[Message]) => {
        val resolver = ctx.messagesResolver
        resolver.getMessagesBySenders(ids(messageBySender)).flatMap(resolver.getMessages)
      }
    )

  val cachedMessageResolver: Fetcher[MainResolver, Message, Message, Long] = Fetcher.rel(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => fetchMessagesSequentially(ctx, ids),
    fetchRel = (ctx: MainResolver, ids: RelationIds[Message]) => {
      println(s"Cached resolver: ${ids.rawIds.values.flatten}")
      val resolver = ctx.messagesResolver
      val seq = ids(messageBySender).map(resolver.messagesRepository.getBySender)
      runManySeq(seq).map(_.flatten).flatMap(fetchMessagesSequentially(ctx, _))
    }
  )

  val batchedCachedMessageResolver: Fetcher[MainResolver, Message, Message, Long] = Fetcher.rel(
    config = FetcherConfig.caching(FetcherCache.simple),
    fetch = (ctx: MainResolver, ids: Seq[Long]) => ctx.messagesResolver.getMessages(ids.toList),
    fetchRel = (ctx: MainResolver, ids: RelationIds[Message]) => {
      val resolver = ctx.messagesResolver
      resolver.getMessagesBySenders(ids(messageBySender)).flatMap(resolver.getMessages)
    }
  )

  private def fetchMessagesSequentially(ctx: MainResolver, ids: Seq[Long]) =
    runManySeq(ids.map(id => ctx.messagesResolver.messagesRepository.getMessage(id).map(_.head)))
}
