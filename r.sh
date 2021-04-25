for i in 1 2 3 4 5 .. 10
do
  sbt "runMain benchmark.Run async q3"
  sbt "runMain benchmark.Run batched q3"
  sbt "runMain benchmark.Run cached q3"
  sbt "runMain benchmark.Run batchedcached q3"
done