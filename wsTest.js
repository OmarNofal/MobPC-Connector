const ws = require('ws');

const client = new ws('ws://localhost:6543');

client.on('open', () => {
  // Causes the server to print "Hello"
  client.send('C://Users//omarw//');
});

client.on('message', (data) => {
    console.log(data.toString());
})

setTimeout(
    () => { client.send('~/Music/')},
    5000
);