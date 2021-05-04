package benchmark

import benchmark.BenchmarkQueries.{Strategies, q3, q4}
import benchmark.Execution.{ex, stop}
import benchmark.HttpClient.handleResponse
import benchmark.Utils.{resolveStrategy, saveToFile}
import io.circe.Json

import scala.util.{Failure, Success}

object Run extends App {

  val q =
    s"""
       |{
       |   person${strategyToUse}(id: 20) {
       |    knows {
       |      messages {
       |        id
       |        content
       |      }
       |    }
       |  }
       |}
       |""".stripMargin
  //  strategyToUse = Strategies.Async
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
  val res = executor
    .parseQuery(q4(strategyToUse).body)
    .map(executor.executeQuery)
    .map(_.map(handleResponse(_, queryToUse.name, strategyToUse.toString)))
  //  val queryToUse = BenchmarkQueries.q2(strategyToUse)
  //  (1 to 10).map(_ => {
  val beforeUsedMem: Long = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
  //  val strategyToUse = Strategies.Async
  var (strategyToUse, queryToUse) = resolveStrategy(args)

  res.toOption.map(
    _.onComplete {
      case Success(value) => {
        println(value)
        val afterUsedMem = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
        saveToFile(value, "4", strategyToUse.toString, "results_4");
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
