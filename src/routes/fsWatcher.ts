const ws = require('ws');
const fs = require('fs');
const { parsePath } = require('../fs/operations');

const server = new ws.Server({noServer: true});

server.on('connection', socket => {  

    
  var watcher = null

  socket.on('message', message => {
    const resource = parsePath(message.toString());
    console.log("User wants to watch " + resource);

    if (watcher != null)
        watcher.close();

    try {
      watcher = fs.watch(resource);
    } catch (e) {
      console.log(e);
      socket.send('unavailable');
    }
    watcher.on('change', (eventType, fileName) => {
        console.log("Directory or file changed");
        socket.send('changed');
    });

    watcher.on('error', error => {
        socket.send('file deleted');
    });

    socket.send('ok');
  });

  socket.on('close', () => {
    watcher.close()
  });

  socket.on('error', () => {
    watcher.close()
  });

});


module.exports = server;