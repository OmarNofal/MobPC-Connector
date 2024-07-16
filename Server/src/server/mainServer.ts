import express from 'express'
import http from 'http'
import https from 'https'
import net from 'net'
import path from 'path'
import { BehaviorSubject, fromEvent, map, merge } from 'rxjs'
import AuthorizationManager from '../auth/auth'
import credentialsManager from '../credentials/CredentialsManager'
import { MainServerState } from '../model/mainServerState'
import { ServerInformation } from '../model/preferences'
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
import createProxyServer from './proxy'

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
     * This is the proxy server which forwards
     * the request to the http or https server depending on the request.
     *
     * This server is used so that we need to listen only on one port instead of two
     */
    private proxyServer: net.Server

    /**
     * The main `https` server which the client devices
     * mainly interact with. It uses a self-signed certificate
     * to secure communication.
     */
    private httpsServer: https.Server

    /**
     * The http server that serves unencrypted content
     * to external apps. These apps normally reject
     * loading content over self-signed certificate,
     * and so this http server mitigates this issue
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
     * The current information of the server
     */
    serverInformation: BehaviorSubject<ServerInformation>

    /**
     *
     * @param appDirectory The directory of the app configuration files. This is used
     * to create a folder to store temporarily uploaded files
     * @param authManager An authentication manager to check for authentication and authorization
     */
    constructor(
        appDirectory: string,
        authManager: AuthorizationManager,
        serverInformation: BehaviorSubject<ServerInformation>
    ) {
        this.serverInformation = serverInformation
        this.setupServers(appDirectory, authManager)
        this.setupObservables()
    }

    /**
     * Starts running all servers
     *
     * Note: The `fsWatcherServer` is always running and waiting to upgrade connections
     */
    run = () => {
        this.proxyServer.listen(6543)
    }

    /**
     * Terminates all connections and closes all servers
     * immediately
     */
    stop = () => {
        this.httpServer.closeAllConnections()
        this.httpsServer.closeAllConnections()

        this.proxyServer.close()
    }

    /**
     * @returns true if the server is listening for connections
     */
    isRunning = () => {
        return this.proxyServer.listening
    }

    setupObservables = () => {
        const closeEvent = fromEvent(this.proxyServer, 'close').pipe(map(() => 'closed'))
        const openEvent = fromEvent(this.proxyServer, 'listening').pipe(map(() => 'listening'))
        const errorEvent = fromEvent(this.proxyServer, 'error').pipe(map(() => 'error'))

        // holds the current state of the express server (opened or closed or error)
        const proxyServerObservable = merge(closeEvent, openEvent, errorEvent)

        this.state = new BehaviorSubject<MainServerState>({
            state: 'closed',
            port: 6543,
            serverName: 'Omar Walid',
        })
        proxyServerObservable
            .pipe(
                map((val): MainServerState => {
                    if (val == 'listening') {
                        return {
                            state: 'running',
                            port: 6543,
                            serverName: 'Omar Walid',
                        }
                    } else {
                        return {
                            state: 'closed',
                            port: 6543,
                            serverName: 'Omar Walid',
                        }
                    }
                })
            )
            .subscribe(this.state)
    }

    private setupServers = (appDirectory: string, authManager: AuthorizationManager) => {
        this.httpsServer = this.createHTTPSServer(appDirectory, authManager)
        this.httpServer = this.createHTTPServer(authManager)

        this.fsWatcherServer = new FileSystemWatcherService()

        this.httpServer.on('upgrade', (request, socket, head) => {
            console.log('Client wants to watch -___-')
            this.fsWatcherServer.handleIncomingConnection(request, socket, head)
        })

        this.proxyServer = createProxyServer(this.httpServer, this.httpsServer)
    }

    private createHTTPSServer = (appDirectory: string, authManager: AuthorizationManager) => {
        const authMiddleware = createAuthMiddlewareFunction(authManager.isValidDeviceToken)

        const app = express()
        app.use(express.urlencoded({ extended: true }))

        addDownloadRoutes(
            app,
            authMiddleware,
            authManager.createFileAccessToken,
            authManager.getPathFromFileAccessToken
        )
        addDirectoryRoutes(app, authMiddleware)
        addClipboardRoutes(app, authMiddleware)
        addBrowserRoutes(app, authMiddleware)
        addStatusRoutes(app, () => this.serverInformation.value.uuid)
        addOSRoutes(app, authMiddleware)
        addFileOperationsRoutes(app, authMiddleware)
        addAuthRoutes(app, authManager)

        const uploadTemporaryPath = path.join(appDirectory, 'uploadTemp')
        addUploadRoutes(app, uploadTemporaryPath, authMiddleware)

        const credentials = this.getServerCredentials()
        const httpsServer = https.createServer({ key: credentials.privateKey, cert: credentials.cert }, app)

        return httpsServer
    }

    private createHTTPServer = (authManager: AuthorizationManager) => {
        const authMiddleware = createAuthMiddlewareFunction(authManager.isValidDeviceToken)

        const app = express()

        app.use(express.urlencoded({ extended: true }))
        addDownloadRoutes(
            app,
            authMiddleware,
            authManager.createFileAccessToken,
            authManager.getPathFromFileAccessToken
        )
        addStatusRoutes(app, () => this.serverInformation.value.uuid)

        const httpServer = http.createServer(app)

        return httpServer
    }

    private getServerCredentials = credentialsManager.getCredentials
}
