import electronApp from 'electron'
import EventEmitter from 'events'
import express from 'express'
import fs from 'fs'
import http from 'http'
import https from 'https'
import path from 'path'
import { BehaviorSubject } from 'rxjs'
import AuthenticationManager from './auth/auth.js'
import DetectionServer from './detectionserver.js'
import FirebaseIPService, { FirebaseServiceConfiguration } from './firebase/firebase.js'
import PreferencesManager from './preferences/PreferencesManager.js'
import createAuthMiddlewareFunction from './routes/authMiddleware.js'
import addAuthRoutes from './routes/authRoutes.js'
import addBrowserRoutes from './routes/browserRoutes.js'
import addClipboardRoutes from './routes/clipboardRoutes.js'
import addDirectoryRoutes from './routes/directoryRoutes.js'
import addDownloadRoutes from './routes/downloadRoutes.js'
import { addFileOperationsRoutes } from './routes/fileOperationsRoutes.js'
import FileSystemWatcherService from './routes/fsWatcher.js'
import addOSRoutes from './routes/osRoutes.js'
import addStatusRoutes from './routes/statusRoutes.js'
import addUploadRoutes from './routes/uploadRoutes.js'
import { mapBehaviorSubject } from './utilities/rxUtils.js'


var privateKey  = fs.readFileSync('src/cert/server.key', 'utf8')
var certificate = fs.readFileSync('src/cert/server.crt', 'utf8')

const credentials = {key: privateKey, cert: certificate}


const PORT = 6543

const app = express()

const appDirectory = electronApp.app.getPath('userData')

const authManager = new AuthenticationManager(appDirectory)
const authMiddleware = createAuthMiddlewareFunction(authManager.isValidToken)

app.use(express.json())
app.use(express.urlencoded({extended: true}))
addDownloadRoutes(app, authMiddleware)
addDirectoryRoutes(app, authMiddleware)
addClipboardRoutes(app, authMiddleware)
addBrowserRoutes(app, authMiddleware)
addStatusRoutes(app)
addOSRoutes(app, authMiddleware)
addFileOperationsRoutes(app, authMiddleware)
addUploadRoutes(app, path.join(appDirectory, 'uploadTemp'), authMiddleware)
addAuthRoutes(app, authManager)




let preferencesManager = new PreferencesManager(path.join(appDirectory, "preferences"))



let detectionServerConfiguration = mapBehaviorSubject(preferencesManager.currentPreferences, v => v.detectionServerPrefs)
let detectionServer = new DetectionServer(detectionServerConfiguration)

let firebaseServerConfig = mapBehaviorSubject(preferencesManager.currentPreferences, v => v.firebaseServicePrefs)
let firebaseService = new FirebaseIPService(firebaseServerConfig)

export const events = new EventEmitter();

let https_server = null;
let http_server = null;

export function startServer() {
    detectionServer.run()
    firebaseService.startService()
    http_server = http.createServer(app).listen(6544);
    https_server = httpsServer.listen(PORT);
    events.emit('server-started');
}

export function stopServer() {
    if (https_server != null) {
        detectionServer.close()
        firebaseService.stopService()

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


const fileSystemWatcherService = new FileSystemWatcherService()

const httpsServer = https.createServer(credentials, app);

httpsServer.on('upgrade', (request, socket, head) => {
    fileSystemWatcherService.handleIncomingConnection(request, socket, head)
});

startServer()