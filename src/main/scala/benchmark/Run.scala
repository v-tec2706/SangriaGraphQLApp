package benchmark

import benchmark.BenchmarkQueries.{Strategies, all}
import benchmark.Execution.{ex, stop}
import benchmark.Utils.resolveStrategy
import io.circe.Json

import scala.concurrent.Future

object Run extends App {

  val strategyToUse = resolveStrategy(args).getOrElse(Strategies.Async)

  println(s"Using strategy: $strategyToUse")
  Future
    .sequence(
      all(strategyToUse)
        .map { case (name, q) =>
          (
            name, {
            val executor = ExecutorProvider.provide(strategyToUse)
            executor.parseQuery(q).map(executor.executeQuery)
          }
          )
        }
        .map { case (name, res) => res.fold(error => Future(println(s"Error occurred: $error")), res => res.map(processResult(name, _))) }
    )
    .onComplete(_ => stop())

  //    execution.graphql(BenchmarkQueries.q).onComplete(x => {
  //      println(x)
  //      processResult("q2", x)
  //    })

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
