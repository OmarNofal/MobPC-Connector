const express = require('express');
const https = require('https');
const statusRoutes = require('./routes/statusRoutes.js');
const browserRoutes = require('./routes/browserRoutes.js');
const clipboardRoutes = require('./routes/clipboardRoutes.js');
const directoryRoutes = require('./routes/directoryRoutes.js');
const osRoutes = require('./routes/osRoutes.js');
const fileOperationsRoutes = require('./routes/fileOperationsRoutes.js');
const uploadRoutes = require('./routes/uploadRoutes.js');
const downloadRoutes = require('./routes/downloadRoutes.js');
const loginRouter = require('./routes/authRoutes.js')
const wsServer = require('./routes/fsWatcher.js')
const startFirebaseService = require('./firebase/firebase.js')
const fs = require('fs');
const http = require('http');
const EventEmitter = require('events');
const { changePassword } = require('./auth/auth.js');


var privateKey  = fs.readFileSync('cert/server.key', 'utf8');
var certificate = fs.readFileSync('cert/server.crt', 'utf8');

const credentials = {key: privateKey, cert: certificate};


const {runDetectionServer, closeDetectionServer} = require('./detectionserver.js')

const PORT = 6543

const app = express();

const router = express.Router();

app.use(express.json());
app.use(express.urlencoded({extended: true}))
app.use(downloadRoutes);
app.use(directoryRoutes);
app.use(clipboardRoutes);
app.use(browserRoutes);
app.use(statusRoutes);
app.use(osRoutes);
app.use(fileOperationsRoutes);
app.use(uploadRoutes);
app.use(loginRouter);
app.use(router);


const events = new EventEmitter();

let https_server = null;
let http_server = null;

function startServer(port) {
    runDetectionServer();
    startFirebaseService();
    http_server = http.createServer(app).listen(6544);
    https_server = httpsServer.listen(PORT);
    events.emit('server-started');
}

function stopServer(port) {
    if (https_server != null) {
        closeDetectionServer();

        http_server.close();
        http_server = null;

        https_server.close();
        https_server = null;
        
        events.emit('server-closed');
    }
}

function isServerOpen() {
  return https_server != null
}



const httpsServer = https.createServer(credentials, app);
``
httpsServer.on('upgrade', (request, socket, head) => {
  wsServer.handleUpgrade(request, socket, head, socket => {
    wsServer.emit('connection', socket, request);
  });
});

startServer(4043);

module.exports = {
  startServer, stopServer, events, isServerOpen
}