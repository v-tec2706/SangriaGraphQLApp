FROM java:8

WORKDIR .

COPY target/scala-2.13/SangriaGraphQLApp-assembly-0.1.jar /

CMD java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.data.DataGenerator; java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run async; java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run batched; java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run cached; java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run batchedCached