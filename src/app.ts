import { Menu, Notification, Tray, app, ipcMain, screen } from 'electron'
import path from 'path'
import AuthorizationManager from './auth/auth'
import FirebaseIPService from './firebase/firebase'
import { DetectionServerState } from './model/detectionServerState'
import PreferencesManager from './preferences/PreferencesManager'
import DetectionServer from './server/detectionServer'
import MainServer from './server/mainServer'
import storage from './storage'
import { mapBehaviorSubject } from './utilities/rxUtils'
import AppWindow from './window/appWindow'

import clipboardImage from './ui/static/clipboard.png'
import { START_SERVER_COMMAND, STOP_SERVER_COMMAND } from './bridges/mainServerBridge'
import observeNetworkInterfaces, { NetworkInterface } from './utilities/networkInterfaces'
import { BehaviorSubject } from 'rxjs'
import { GENERATE_PAIRING_PAYLOAD } from './bridges/authBridges'
import generatePairingPayload from './service/pairing/pairing'

declare const MAIN_WINDOW_WEBPACK_ENTRY: string
declare const MAIN_WINDOW_PRELOAD_WEBPACK_ENTRY: string

if (process.platform === 'win32') {
    app.setAppUserModelId(app.name)
}

/**
 * This is the main application class.
 *
 * It contains the entire state of the application,
 * and additionaly contains the `AppWindow` object
 * if it is open
 *
 * This must be only instantiated once
 */
export default class Application {
    authManager: AuthorizationManager

    prefsManager: PreferencesManager

    detectionServer: DetectionServer

    mainServer: MainServer

    firebaseService: FirebaseIPService

    window: AppWindow

    tray: Tray

    networkInterfacesObservable: BehaviorSubject<NetworkInterface[]>

    constructor() {}

    init = () => {
        const appDirectory = app.getPath('userData')

        const authManager = new AuthorizationManager(appDirectory)
        let preferencesManager = new PreferencesManager(path.join(appDirectory, 'app_preferences'))

        let detectionServerConfiguration = mapBehaviorSubject(
            preferencesManager.currentPreferences,
            (v) => v.detectionServerPrefs
        )

        let serverInformation = mapBehaviorSubject(
            preferencesManager.currentPreferences,
            (v) => v.serverInformation
        )

        let detectionServer = new DetectionServer(detectionServerConfiguration, serverInformation)

        let firebaseServerConfig = mapBehaviorSubject(
            preferencesManager.currentPreferences,
            (v) => v.firebaseServicePrefs
        )
        let firebaseService = new FirebaseIPService(firebaseServerConfig)

        const mainServer = new MainServer(appDirectory, authManager)

        this.authManager = authManager
        this.prefsManager = preferencesManager

        this.detectionServer = detectionServer
        this.firebaseService = firebaseService

        this.mainServer = mainServer

        this.networkInterfacesObservable = observeNetworkInterfaces()

        app.whenReady().then(() => {
            this.registerIpcEvents()
            storage.init()
            //storage.changePassword('00000023');

            ipcMain.on('toggle-server', () => {
                if (detectionServer.state.value == DetectionServerState.RUNNING) detectionServer.close()
                else this.detectionServer.run()
            })

            this.showWindow()
            this.setupTray()
        })

        app.on('window-all-closed', (e) => e.preventDefault())
    }

    setupTray = () => {
        this.tray = new Tray(path.resolve(clipboardImage))
        const contextMenu = Menu.buildFromTemplate([])
        this.tray.setToolTip('PC Connector')
        this.tray.setContextMenu(contextMenu)

        this.tray.on('click', () => this.showWindow())
    }

    runServers = () => {
        this.mainServer.run()
        this.detectionServer.run()
        this.firebaseService.startService()
    }

    showWindow = () => {
        if (this.window != undefined && !this.window.isDestroyed()) {
            this.window.show()
            return
        }

        const primaryDisplay = screen.getPrimaryDisplay()
        const width = primaryDisplay.size.width
        const height = primaryDisplay.size.height

        const window = new AppWindow(
            {
                width: width,
                height: height,
                webPreferences: {
                    preload: MAIN_WINDOW_PRELOAD_WEBPACK_ENTRY,
                },
                show: true,
                hasShadow: true,
            },
            [
                // the observables that are pushed through IPC to the browser window to be rendered by the UI
                {
                    observable: this.detectionServer.state,
                    channelName: 'detection-server-state',
                },
                {
                    observable: this.mainServer.state,
                    channelName: 'main-server-state',
                },
                {
                    observable: this.networkInterfacesObservable,
                    channelName: 'network-interfaces-state',
                },
            ]
        )
        this.window = window
        window.loadURL(MAIN_WINDOW_WEBPACK_ENTRY)

        window.on('close', () => {
            let notification = new Notification({
                title: 'Server running',
                body: 'The app is running in the background',
                silent: true,
                subtitle: 'Pc Connector',
            })
            notification.show()
        })
        if (!app.isPackaged) window.webContents.openDevTools()
    }

    registerIpcEvents = () => {
        ipcMain.on(START_SERVER_COMMAND, this.mainServer.run)
        ipcMain.on(STOP_SERVER_COMMAND, this.mainServer.stop)

        ipcMain.handle(GENERATE_PAIRING_PAYLOAD, () => {
            const payload = generatePairingPayload(
                this.authManager,
                this.prefsManager,
                this.networkInterfacesObservable.value
            )
            return JSON.stringify(payload)
        })

        ipcMain.on('send-subject-latest-value', (event, name: string) => {
            const webContents = event.sender

            if (name == 'main-server-state') {
                webContents.send(name, this.mainServer.state.value)
            }
            if (name == 'network-interfaces-state') {
                webContents.send(name, this.networkInterfacesObservable.value)
            }
        })
    }
}
