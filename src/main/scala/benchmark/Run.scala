package benchmark

import benchmark.BenchmarkQueries.Strategies
import benchmark.Execution.{ex, stop}
import benchmark.HttpClient.handleResponse
import benchmark.Utils.saveToFile
import io.circe.Json

import scala.util.{Failure, Success}

object Run extends App {

  val strategyToUse = Strategies.Async
  //  val strategyToUse = resolveStrategy(args).getOrElse(Strategies.Async)

  println(s"Using strategy: $strategyToUse")
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
  val queryToUse = BenchmarkQueries.q1(strategyToUse)

  (1 to 10).map(_ => {
    val res = executor
      .parseQuery(queryToUse.body)
      .map(executor.executeQuery)
      .map(_.map(handleResponse(_, queryToUse.name, strategyToUse.toString)))

    res.toOption.map(
      _.onComplete {
        case Success(value) => println(value); saveToFile(value, queryToUse.name, strategyToUse.toString); stop()
        case Failure(exception) => println(s"Error occurred: $exception"); stop()
      }
    )
  })

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
