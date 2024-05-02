import express from 'express'
import http from 'http'
import https from 'https'
import path from 'path'
import { BehaviorSubject, fromEvent, map, merge } from 'rxjs'
import AuthenticationManager from '../auth/auth'
import CredentialsManager from '../credentials/CredentialsManager'
import { MainServerState } from '../model/mainServerState'
import createAuthMiddlewareFunction from '../routes/authMiddleware'
import addAuthRoutes from '../routes/authRoutes'
import addBrowserRoutes from '../routes/browserRoutes'
import addClipboardRoutes from '../routes/clipboardRoutes'
import addDirectoryRoutes from '../routes/directoryRoutes'
import addDownloadRoutes from '../routes/downloadRoutes'
import { addFileOperationsRoutes } from '../routes/fileOperationsRoutes'
import FileSystemWatcherService from '../routes/fsWatcher'
import addOSRoutes from '../routes/osRoutes'
import addStatusRoutes from '../routes/statusRoutes'
import addUploadRoutes from '../routes/uploadRoutes'
import { getUUID } from '../storage'

/**
 * This is the class which manages the server
 * that the user connects to, to perfrom operations on the PC.
 *
 * It contains 3 main servers:
 * 1. An https server where almost all operations are performed on
 * 2. An http server that is used to server content to apps outside the client app
 * (These apps reject the SSL certificate because it is self-signed, and so, they refuse to load the content sometimes)
 * This server is made to allow file transfer to external apps without using any encryption
 * 3. A websocket server to watch for folder changes and notify observer devices of the change
 */
export default class MainServer {
    /**
     * The http server that serves unencrypted content
     * to external apps. These apps normally reject
     * loading content over self-signed certificate,
     * and so this http server mitigates this issue
     */
    private httpsServer: https.Server

    /**
     * The main `https` server which the client devices
     * mainly interact with. It uses a self-signed certificate
     * to secure communication.
     */
    private httpServer: http.Server

    /**
     * The file watcher service that allows
     * clients to observe folder changes
     * to provide real-time folder changes data.
     */
    private fsWatcherServer: FileSystemWatcherService

    /**
     * This behavior subject contains the current state
     * of the server
     */
    state: BehaviorSubject<MainServerState>

    /**
     *
     * @param appDirectory The directory of the app configuration files. This is used
     * to create a folder to store temporarily uploaded files
     * @param authManager An authentication manager to check for authentication and authorization
     */
    constructor(appDirectory: string, authManager: AuthenticationManager) {
        this.setupServers(appDirectory, authManager)
        this.setupObservables()
    }

    /**
     * Starts running all servers
     *
     * Note: The `fsWatcherServer` is always running and waiting to upgrade connections
     */
    run = () => {
        this.httpsServer.listen(6543)
        this.httpServer.listen(6544)
    }

    /**
     * Terminates all connections and closes all servers
     * immediately
     */
    stop = () => {
        this.httpServer.closeAllConnections()
        this.httpServer.close()

        this.httpsServer.closeAllConnections()
        this.httpsServer.close()
    }

    /**
     * @returns true if the server is listening for connections
     */
    isRunning = () => {
        return this.httpsServer.listening
    }

    setupObservables = () => {
        const closeEvent = fromEvent(this.httpsServer, 'close').pipe(map(() => 'closed'))
        const openEvent = fromEvent(this.httpsServer, 'listening').pipe(map(() => 'listening'))
        const errorEvent = fromEvent(this.httpsServer, 'error').pipe(map(() => 'error'))

        // holds the current state of the express server (opened or closed or error)
        const httpsServerObservable = merge(closeEvent, openEvent, errorEvent)

        this.state = new BehaviorSubject<MainServerState>('init')
        httpsServerObservable
            .pipe(
                map((val): MainServerState => {
                    if (val == 'listening') {
                        return {
                            state: 'running',
                            httpPort: 6544,
                            httpsPort: 6543,
                            serverName: 'Omar Walid',
                        }
                    } else {
                        return {
                            state: 'closed',
                            httpPort: 6544,
                            httpsPort: 6543,
                            serverName: 'Omar Walid',
                        }
                    }
                })
            )
            .subscribe(this.state)
    }

    private setupServers = (appDirectory: string, authManager: AuthenticationManager) => {
        const authMiddleware = createAuthMiddlewareFunction(authManager.isValidToken)

        const app = express()
        app.use(express.urlencoded({ extended: true }))

        addDownloadRoutes(app, authMiddleware)
        addDirectoryRoutes(app, authMiddleware)
        addClipboardRoutes(app, authMiddleware)
        addBrowserRoutes(app, authMiddleware)
        addStatusRoutes(app, getUUID)
        addOSRoutes(app, authMiddleware)
        addFileOperationsRoutes(app, authMiddleware)
        addAuthRoutes(app, authManager)

        const uploadTemporaryPath = path.join(appDirectory, 'uploadTemp')
        addUploadRoutes(app, uploadTemporaryPath, authMiddleware)

        const credentials = this.getServerCredentials()
        this.httpServer = http.createServer(app)
        this.httpsServer = https.createServer({ key: credentials.privateKey, cert: credentials.cert }, app)
        this.fsWatcherServer = new FileSystemWatcherService()

        //this.registerCallbacks()
        this.httpServer.on('upgrade', (request, socket, head) => {
            console.log('Client wants to watch -___-')
            this.fsWatcherServer.handleIncomingConnection(request, socket, head)
        })
    }

    private credentialsManager = new CredentialsManager()
    private getServerCredentials = this.credentialsManager.getCredentials
}
