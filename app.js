const express = require('express');
const https = require('https');
const statusRoutes = require('./routes/statusRoutes.js');
const browserRoutes = require('./routes/browserRoutes');
const clipboardRoutes = require('./routes/clipboardRoutes');
const directoryRoutes = require('./routes/directoryRoutes');
const osRoutes = require('./routes/osRoutes');
const fileOperationsRoutes = require('./routes/fileOperationsRoutes');
const uploadRoutes = require('./routes/uploadRoutes');
const downloadRoutes = require('./routes/downloadRoutes');
const loginRouter = require('./routes/authRoutes.js')
const wsServer = require('./routes/fsWatcher.js')
const auth = require("./auth/auth.js");
const authExceptions = require('./auth/exceptions.js');
const startFirebaseService = require('./firebase/firebase.js')
const fs = require('fs');
const { changePassword } = require('./auth/auth.js');
const http = require('http');

var privateKey  = fs.readFileSync('cert/server.key', 'utf8');
var certificate = fs.readFileSync('cert/server.crt', 'utf8');

const credentials = {key: privateKey, cert: certificate};


const {runDetectionServer, closeDetectionServer} = require('./detectionserver')


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




http.createServer(app).listen(6544);

const httpsServer = https.createServer(credentials, app);

httpsServer.listen(PORT)

runDetectionServer();

startFirebaseService();

httpsServer.on('upgrade', (request, socket, head) => {
  wsServer.handleUpgrade(request, socket, head, socket => {
    wsServer.emit('connection', socket, request);
  });
});