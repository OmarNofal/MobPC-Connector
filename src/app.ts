import { BrowserWindow, app, ipcMain, screen } from 'electron'
import path from 'path'
import AuthenticationManager from './auth/auth'
import FirebaseIPService from './firebase/firebase'
import PreferencesManager from './preferences/PreferencesManager'
import DetectionServer from './server/detectionServer'
import MainServer from './server/mainServer'
import storage from './storage'
import { pipeObservableToIPC } from './utilities/rxIPC'
import { mapBehaviorSubject } from './utilities/rxUtils'
import { DetectionServerState } from './model/detectionServerState'
import AppWindow from './window/appWindow'





declare const MAIN_WINDOW_WEBPACK_ENTRY: string;
declare const MAIN_WINDOW_PRELOAD_WEBPACK_ENTRY: string;

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


    authManager: AuthenticationManager

    prefsManager: PreferencesManager

    detectionServer: DetectionServer

    mainServer: MainServer

    firebaseService: FirebaseIPService


    window: AppWindow

    constructor() { }


    init = () => {

        const appDirectory = app.getPath('userData')

        const authManager = new AuthenticationManager(appDirectory)
        let preferencesManager = new PreferencesManager(path.join(appDirectory, "app_preferences"))

        let detectionServerConfiguration = mapBehaviorSubject(
            preferencesManager.currentPreferences, 
            v => v.detectionServerPrefs
        )
        let detectionServer = new DetectionServer(detectionServerConfiguration)


        let firebaseServerConfig = mapBehaviorSubject(
            preferencesManager.currentPreferences, 
            v => v.firebaseServicePrefs
        )
        let firebaseService = new FirebaseIPService(firebaseServerConfig)

        const mainServer = new MainServer(appDirectory, authManager)

        this.authManager = authManager
        this.prefsManager = preferencesManager

        this.detectionServer = detectionServer
        this.firebaseService = firebaseService

        this.mainServer = mainServer



        app.whenReady().then(
            () => {
                storage.init();
                //storage.changePassword('00000023');

                ipcMain.on(
                    'toggle-server',
                    () => {
                        if (detectionServer.state.value == DetectionServerState.RUNNING)
                            detectionServer.close() 
                        else this.detectionServer.run()
                    }
                )

                this.showWindow();
            });

        app.on('window-all-closed', app.quit);

    }

    runServers = () => {
        this.mainServer.run()
        this.detectionServer.run()
        this.firebaseService.startService()
    }

    showWindow = () => {

        const primaryDisplay = screen.getPrimaryDisplay()
        const width = primaryDisplay.size.width / 2
        const height = primaryDisplay.size.height / 2

        const window = new AppWindow(
            {
                width: width,
                height: height,
                webPreferences: {
                    preload: MAIN_WINDOW_PRELOAD_WEBPACK_ENTRY
                },
                show: true,
                hasShadow: true
            }, 
            [
                {
                    observable: this.detectionServer.state,
                    channelName: 'detection-server-state'
                },

            ]
        );
    
        window.loadURL(MAIN_WINDOW_WEBPACK_ENTRY);

        if (!app.isPackaged) window.webContents.openDevTools()
    }



}

