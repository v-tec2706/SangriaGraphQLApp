const {ApolloGateway} = require('@apollo/gateway');
const {Server} = require('./server');

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function run() {
    console.log("waiting for start...")
    await sleep(10000)
    console.log("started...")

    const gateway = new ApolloGateway({
        serviceList: [
            {name: 'personAsync', url: 'http://asyncserver:8081/graphql'},
            {name: 'personBatched', url: 'http://batchedserver:8082/graphql'},
            {name: 'personCached', url: 'http://cachedserver:8083/graphql'},
            {name: 'personBatchedCached', url: 'http://batchedcachedserver:8084/graphql'}
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
}

run()
