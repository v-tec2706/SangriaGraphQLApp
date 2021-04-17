const {ApolloGateway} = require('@apollo/gateway');
const {Server} = require('./server');

const gateway = new ApolloGateway({
  serviceList: [
    {name: 'personAsync', url: 'http://localhost:8081/graphql'},
    {name: 'personBatched', url: 'http://localhost:8082/graphql'},
    {name: 'personCached', url: 'http://localhost:8083/graphql'},
    {name: 'personBatchedCached', url: 'http://localhost:8084/graphql'}
  ],
  debug: true
});

const port = 9080;

(async () => {

  const server = new Server({
    gateway,
    subscriptions: false
  });

  server.listen({port: 9080}).then(({url}) => {
    console.log(`🚀 Server ready at ${url}`)
  })
})();
