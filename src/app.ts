import express from 'express';
import https from 'https';
import statusRoutes from './routes/statusRoutes.js';
import browserRoutes from './routes/browserRoutes.js';
import clipboardRoutes from './routes/clipboardRoutes.js';
import directoryRoutes from './routes/directoryRoutes.js';
import osRoutes from './routes/osRoutes.js';
import fileOperationsRoutes from './routes/fileOperationsRoutes.js';
import uploadRoutes from './routes/uploadRoutes.js';
import downloadRoutes from './routes/downloadRoutes.js';
import loginRouter from './routes/authRoutes.js';
import wsServer from './routes/fsWatcher.js';
import startFirebaseService from './firebase/firebase.js';
import fs from 'fs';
import http from 'http';
import EventEmitter from 'events';


var privateKey  = fs.readFileSync('src/cert/server.key', 'utf8');
var certificate = fs.readFileSync('src/cert/server.crt', 'utf8');

const credentials = {key: privateKey, cert: certificate};


import DetectionServer, { DetectionServerConfiguration, DetectionServerState } from './detectionserver.js';
import { BehaviorSubject } from 'rxjs';

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



let detectionServerConfiguration = new BehaviorSubject<DetectionServerConfiguration>({portNumber: 4285})
let detectionServer = new DetectionServer(detectionServerConfiguration);

export const events = new EventEmitter();

let https_server = null;
let http_server = null;

export function startServer() {
    detectionServer.run()
    startFirebaseService();
    http_server = http.createServer(app).listen(6544);
    https_server = httpsServer.listen(PORT);
    events.emit('server-started');
}

export function stopServer() {
    if (https_server != null) {
        detectionServer.close()

        http_server.close();
        http_server = null;

        https_server.close();
        https_server = null;
        
        events.emit('server-closed');
    }
}

export function isServerOpen() {
  return https_server != null
}


const httpsServer = https.createServer(credentials, app);
``
httpsServer.on('upgrade', (request, socket, head) => {
  wsServer.handleUpgrade(request, socket, head, socket => {
    wsServer.emit('connection', socket, request);
  });
});

startServer()

setTimeout(() => {
  detectionServer.close()
  setTimeout(detectionServer.run, 3000)
}, 3000)

