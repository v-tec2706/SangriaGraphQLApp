package benchmark

import benchmark.Execution.{ex, stop}
import benchmark.HttpClient.handleResponse
import benchmark.Utils.{resolveStrategy, saveToFile}
import io.circe.Json

import scala.util.{Failure, Success}

object Run extends App {

  //  val strategyToUse = Strategies.Async
  val (strategyToUse, queryToUse) = resolveStrategy(args)
  //  println(s"Waiting for start...")
  //  System.in.read()
  println(s"Using strategy: $strategyToUse and query: ${queryToUse.name}")
  //  Future
  //    .sequence(
  //      all(strategyToUse)
  //        .map { case (name, q) =>
  //          (
  //            name, {
  //            val executor = ExecutorProvider.provide(strategyToUse)
  //            executor.parseQuery(q).map(executor.executeQuery)
  //          }
  //          )
  //        }
  //        .map { case (name, res) => res.fold(error => Future(println(s"Error occurred: $error")), res => res.map(processResult(name, _))) }
  //    )
  //    .onComplete(_ => stop())

  val executor = ExecutorProvider.provide(strategyToUse)
  //  val queryToUse = BenchmarkQueries.q2(strategyToUse)
  //  (1 to 10).map(_ => {
  val beforeUsedMem: Long = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
  val res = executor
    .parseQuery(queryToUse.body)
    .map(executor.executeQuery)
    .map(_.map(handleResponse(_, queryToUse.name, strategyToUse.toString)))

  res.toOption.map(
    _.onComplete {
      case Success(value) => {
        println(value)
        val afterUsedMem = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
        saveToFile(value, queryToUse.name, strategyToUse.toString);
        saveToFile(s"Memory used: ${(afterUsedMem - beforeUsedMem) / 1000000}\n", queryToUse.name, strategyToUse.toString);
        stop()
      };
      case Failure(exception) => println(s"Error occurred: $exception"); stop()
      //        println(s"Memory used: ${(afterUsedMem - beforeUsedMem) / 1000000} MB")
      //        stop()
      //        println(s"Waiting to exit...")
      //        System.in.read()

    }
  )
  //  val afterUsedMem = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
  //  println(s"Memory used: ${(afterUsedMem - beforeUsedMem) / 1000000} ")
  //  })

  def processResult: (String, Json) => Unit = (name, res) => {
    println(res)
    res.hcursor
      .downField("extensions")
      .downField("tracing")
      .downField("duration")
      .focus
      .map(x => println(s"$name, $x"))
  }
}
