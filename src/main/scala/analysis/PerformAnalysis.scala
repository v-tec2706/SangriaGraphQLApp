package analysis

import benchmark.BenchmarkQueries.Strategies.Strategy
import benchmark.BenchmarkQueries.{Query, Strategies, q1, q2, q4, q5, q6, q7, q8}
import benchmark.Execution.{ex, stop}
import benchmark.ExecutorProvider
import benchmark.HttpClient.{handleResponse, sendGraphQLRequest}
import benchmark.Utils.saveToFile
import benchmark.api.async.QueriesSchema.asyncSchema
import io.circe.parser.parse
import sangria.ast.Field
import sangria.parser.QueryParser
import sangria.schema.Schema

import scala.concurrent.Future

object PerformAnalysis extends App {

  //    .map(_.map(x => (x._1.renderPretty, x._2)))
  //    .map(_.map(x => println(x)))
  //    .map(_.map(x => s"{ ${assignStrategy(x._1, x._2)} }"))
  //    .map(_.map { case (str, value) => runQuery(str) })
  //    .map(
  //      _.map(
  //        sendGraphQLRequest("8081", _)
  //          .map(HttpClient.handleResponse(_, "q1", "async"))
  //      ).map(_.map(println))
  //    )
  val analyzer = new QueryAnalyzer
  val toRun = (q6(Strategies.Async).body :: splitQueries(q6(Strategies.Async), analyzer)).zipWithIndex.map(_.swap)
  //    .foreach(println)
  val queries = List(Strategies.Async)
    //  val queries = List(Strategies.Async, Strategies.Batched, Strategies.Cached, Strategies.BatchedCached)
    .flatMap(s => toRun.map { case (id, q) =>
      runQueryRemote(q.replace("Async", s.toString), id.toString, "remote", s)
    })

  //  println(queries)
  val p: Future[List[Unit]] = Future.sequence(queries)

  p.onComplete(_ => {
    println("Done")
    stop()
  }
  )

  def splitQueries(query: Query, analyzer: QueryAnalyzer, schema: Schema[_, _] = asyncSchema) = {
    val queryTypes = analyzer.getQueryTypes(schema)
    QueryParser
      .parse(query.body)
      .map(analyzer.extractValues)
      .map(_.head.head.asInstanceOf[Field])
      .map(analyzer.splitQuery(_, queryTypes))
      .map(_.map(y => "{\n" + y._1.renderPretty + "\n}"))
      .getOrElse(List.empty)
  }

  def assignStrategy(query: String, labels: Set[PerformAnalysis.QueryTypes.QueryType]): String = query

  def runQueryLocal(query: String, queryId: String, queriesBatch: String, strategy: Strategy): Future[Unit] = {
    val executor = ExecutorProvider.provide(strategy)
    val beforeUsedMem: Long = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
    val res = executor
      .parseQuery(query)
      .map(executor.executeQuery)
      .map(_.map(handleResponse(_, " ", strategy.toString)))
      .toOption
      .getOrElse(Future {
        "failed"
      })

    res.map {
      value => {
        println(value)
        //        val afterUsedMem = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
        saveToFile(value, queriesBatch + "_" + queryId, strategy.toString, s"results_$queriesBatch");
        //        saveToFile(s"Memory used: ${(afterUsedMem - beforeUsedMem) / 1000000}\n", queryToUse.name, strategyToUse.toString);
        //        stop()
      }
      //      case Failure(exception) => println(s"Error occurred: $exception"); stop()
    }

  }

  def runQueryRemote(query: String, queryId: String, queriesBatch: String, strategy: Strategy): Future[Unit] = {
    val res = sendGraphQLRequest("54.156.149.50", "8081", query)
      .map(r => r.asString.map(parse).flatMap(_.toOption).map(handleResponse(_, " ", strategy.toString)))
      .map(_.getOrElse("Execution failed"))

    res.map {
      value => {
        println(value)
        //        val afterUsedMem = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
        saveToFile(value, queriesBatch + "_" + queryId, strategy.toString, s"results_$queriesBatch");
        //        saveToFile(s"Memory used: ${(afterUsedMem - beforeUsedMem) / 1000000}\n", queryToUse.name, strategyToUse.toString);
        //        stop()
      }
      //      case Failure(exception) => println(s"Error occurred: $exception"); stop()
    }
  }

  object QueryTypes extends Enumeration {
    type QueryType = Value
    val Scalar, Object, Collection, Unknown = Value
  }

}
