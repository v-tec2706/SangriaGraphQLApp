echo "Initialize database..."
#java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.data.DataGenerator;
echo "Done"
echo "Starting benchmark ${1} at port: ${2}"
java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.federation.GraphQL ${1} ${2}
#java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run async;
#java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run batched;
#java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run cached;
#java -cp SangriaGraphQLApp-assembly-0.1.jar benchmark.Run batchedCached;