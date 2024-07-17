import { Menu, Notification, Tray, app, clipboard, ipcMain, screen } from 'electron'
import path from 'path'
import AuthorizationManager from './auth/auth'
import FirebaseIPService from './firebase/firebase'
import { DetectionServerState } from './model/detectionServerState'
import PreferencesManager from './preferences/PreferencesManager'
import DetectionServer from './server/detectionServer'
import MainServer from './server/mainServer'
import { mapBehaviorSubject } from './utilities/rxUtils'
import AppWindow from './window/appWindow'
import os from 'os'

import { BehaviorSubject, distinct, map } from 'rxjs'
import trayIcon from '../logo/logo_small.png'
import { DELETE_DEVICE, DEVICE_CONNECTED_EVENT, GENERATE_PAIRING_PAYLOAD } from './bridges/authBridges'
import { START_SERVER_COMMAND, STOP_SERVER_COMMAND } from './bridges/mainServerBridge'
import generatePairingPayload from './service/pairing/pairing'
import observeNetworkInterfaces, { NetworkInterface } from './utilities/networkInterfaces'
import { setupIPCMainPrefsHandler } from './ipc/ipcHandlers'
import {
    APP_BEHAVIOR_PREFS,
    RUN_SERVER_ON_STARTUP,
    SERVER_CONFIGURATION,
    SERVER_INFORMATION,
    START_ON_LOGIN,
} from './model/preferences'
import { setAppStartOnLogin } from './appStartup'

declare const MAIN_WINDOW_WEBPACK_ENTRY: string
declare const MAIN_WINDOW_PRELOAD_WEBPACK_ENTRY: string

if (process.platform === 'win32') {
    app.setAppUserModelId(app.name)
}
if (require('electron-squirrel-startup')) app.quit()

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
        const preferencesManager = new PreferencesManager(path.join(appDirectory, 'app_preferences'))

        let detectionServerConfiguration = mapBehaviorSubject(
            preferencesManager.currentPreferences,
            (v) => v.detectionServerPrefs
        )

        let serverInformation = mapBehaviorSubject(preferencesManager.currentPreferences, (v) => v[SERVER_INFORMATION])
        let serverConfiguration = mapBehaviorSubject(
            preferencesManager.currentPreferences,
            (v) => v[SERVER_CONFIGURATION]
        )

        let detectionServer = new DetectionServer(detectionServerConfiguration, serverInformation, serverConfiguration)

        let firebaseServerConfig = mapBehaviorSubject(
            preferencesManager.currentPreferences,
            (v) => v.firebaseServicePrefs
        )
        let firebaseService = new FirebaseIPService(firebaseServerConfig, serverInformation)

        const mainServer = new MainServer(appDirectory, authManager, serverInformation, serverConfiguration)

        this.authManager = authManager
        this.prefsManager = preferencesManager

        this.detectionServer = detectionServer
        this.firebaseService = firebaseService

        this.mainServer = mainServer

        this.networkInterfacesObservable = observeNetworkInterfaces()

        app.whenReady().then(() => {
            this.registerIpcEvents()
            this.registerEvents()

            ipcMain.on('toggle-server', () => {
                if (detectionServer.state.value == DetectionServerState.RUNNING) detectionServer.close()
                else this.detectionServer.run()
            })

            clipboard.writeText(MAIN_WINDOW_WEBPACK_ENTRY + ' ' + MAIN_WINDOW_PRELOAD_WEBPACK_ENTRY)

            if (!process.argv.includes('--login')) this.showWindow()
            this.setupTray()

            // run server if user set it in the preferences
            if (preferencesManager.currentPreferences.value[APP_BEHAVIOR_PREFS][RUN_SERVER_ON_STARTUP])
                this.runServers()

            const shouldStartOnLogin = preferencesManager.currentPreferences.value[APP_BEHAVIOR_PREFS][START_ON_LOGIN]
            setAppStartOnLogin(shouldStartOnLogin)
        })

        app.on('window-all-closed', (e) => e.preventDefault())

        this.firebaseService.startService()
    }

    setupTray = () => {
        this.tray = new Tray(path.resolve(__dirname, trayIcon))
        const contextMenu = Menu.buildFromTemplate([
            {
                label: 'Exit',
                type: 'normal',
                click: this.quitApp,
            },
        ])
        this.tray.setToolTip('MobPC Connector')
        this.tray.setContextMenu(contextMenu)

        this.tray.on('click', () => this.showWindow())
    }

    runServers = () => {
        this.mainServer.run()
        this.detectionServer.run()
    }

    showWindow = () => {
        if (this.window != undefined && !this.window.isDestroyed()) {
            this.window.show()
            return
        }

        const primaryDisplay = screen.getPrimaryDisplay()
        const width = primaryDisplay.size.width * 0.75
        const height = primaryDisplay.size.height * 0.75

        const window = new AppWindow(
            {
                width: width,
                height: height,
                webPreferences: {
                    preload: MAIN_WINDOW_PRELOAD_WEBPACK_ENTRY,
                },
                show: true,
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
                {
                    observable: this.authManager.devicesDatabase,
                    channelName: 'devices-db-state',
                },
                {
                    observable: this.prefsManager.currentPreferences,
                    channelName: 'prefs-state',
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
                subtitle: 'MobPC Connector',
            })
            if (this.mainServer.isRunning()) notification.show()
        })
        if (!app.isPackaged) window.webContents.openDevTools()
    }

    registerIpcEvents = () => {
        ipcMain.on(START_SERVER_COMMAND, () => {
            this.mainServer.run()
            this.detectionServer.run()
        })
        ipcMain.on(STOP_SERVER_COMMAND, () => {
            this.mainServer.stop()
            this.detectionServer.close()
        })

        ipcMain.handle(GENERATE_PAIRING_PAYLOAD, () => {
            const payload = generatePairingPayload(
                this.authManager,
                this.prefsManager,
                this.networkInterfacesObservable.value
            )
            return JSON.stringify(payload)
        })
        ipcMain.handle(DELETE_DEVICE, (_, id) => this.authManager.deleteDevice(id))

        ipcMain.on('send-subject-latest-value', (event, name: string) => {
            const webContents = event.sender

            if (name == 'main-server-state') {
                webContents.send(name, this.mainServer.state.value)
            }
            if (name == 'network-interfaces-state') {
                webContents.send(name, this.networkInterfacesObservable.value)
            }
            if (name == 'devices-db-state') {
                webContents.send(name, this.authManager.devicesDatabase.value)
            }
            if (name == 'prefs-state') {
                webContents.send(name, this.prefsManager.currentPreferences.value)
            }
        })

        setupIPCMainPrefsHandler(this.prefsManager)
    }

    registerEvents = () => {
        this.authManager.events.subscribe((event) => {
            if (event == 'device_connected') {
                const window = this.window
                if (window != undefined && !window.isDestroyed()) {
                    window.webContents.send(DEVICE_CONNECTED_EVENT)
                }
            }
        })

        this.prefsManager.currentPreferences
            .pipe(map((v) => v[APP_BEHAVIOR_PREFS][START_ON_LOGIN]))
            .subscribe(setAppStartOnLogin)
    }

    quitApp = () => {
        try {
            this.mainServer.stop()
            this.detectionServer.close()
            this.firebaseService.stopService()
            this.window.destroy()
        } catch (e) {
            console.error('Error while cleaning up: ' + e)
        } finally {
            app.quit()
        }
    }
}
