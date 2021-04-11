sbt "run async" | grep "^q" > results/res_async.txt
sbt "run batched" | grep "^q" > results/res_batched.txt
sbt "run cached" | grep "^q" > results/res_cached.txt
sbt "run batchedCached" | grep "^q" > results/res_batchedCached.txt