package benchmark

import benchmark.Execution.ex
import benchmark.api.QueriesSchema
import benchmark.resolver.Resolver

object Run extends App {

  val execution = new Execution(Resolver.build, QueriesSchema.benchmarkQuerySchema)
  val res = execution.graphql(BenchmarkQueries.q1)
  res.foreach(x => {
    println(x)
    Execution.stop()
  })
}
