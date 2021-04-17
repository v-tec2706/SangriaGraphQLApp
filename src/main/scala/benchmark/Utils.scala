package benchmark

import benchmark.BenchmarkQueries.Strategies

object Utils {
  def resolveStrategy(args: Array[String]): Option[Strategies.Strategy] = args.toList match {
    case List(s) => Some(resolveStrategy(s))
    case _ => None
  }

  def resolveStrategy(arg: String): Strategies.Strategy = arg.toLowerCase match {
    case "async" => Strategies.Async
    case "batched" => Strategies.Batched
    case "cached" => Strategies.Cached
    case "batchedcached" => Strategies.BatchedCached
    case _ => println("Unknown strategy, using default"); Strategies.Async
  }
}
