import { randomUUID } from 'crypto'
import { readFileSync, writeFileSync, existsSync, mkdir } from 'fs'
import os from 'os'
import path from 'path'
import { BehaviorSubject, Observable } from 'rxjs'
import * as model from '../model/preferences'

const prefsFileName = 'prefs.json'

/**
 * This class manages the global app's preferences
 * and saves it to disk. The class exposes a `BehaviorSubject` containing
 * all user preferences and allows other components to
 * subscribe to the specific settings they need to observe.
 */
export default class PreferencesManager {
    /**
     * A subject containing state for
     * all the application preferences
     */
    readonly currentPreferences: BehaviorSubject<model.AppPreferences>

    /**
     * The path to the json file containing the preferences
     */
    private readonly prefsFilePath: string

    /**
     *
     * @param prefsDirectory a directory containing `prefs.json` file
     */
    constructor(prefsDirectory: string) {
        this.prefsFilePath = path.join(prefsDirectory, prefsFileName)
        const initalPrefs = PreferencesManager.readAppPreferencesFromDisk(this.prefsFilePath)

        this.currentPreferences = new BehaviorSubject<model.AppPreferences>(initalPrefs)

        PreferencesManager.writeAppDataPreferencesToDisk(this.currentPreferences.value, this.prefsFilePath)
    }

    /**
     * Loads the preference file into a `AppPreferences`
     *
     * If fields are missing, then it will load default values
     * @param prefsPath the path to the preferences json file
     */
    static readAppPreferencesFromDisk(prefsFilePath: string): model.AppPreferences {
        let appPrefsJson
        try {
            const fileContents: string = readFileSync(prefsFilePath, { encoding: 'utf-8' })
            appPrefsJson = JSON.parse(fileContents)
        } catch (e: any) {
            appPrefsJson = {} // this causes it to read default values
        }

        return this.parseAppPrefsFromJson(appPrefsJson)
    }

    /**
     * Writes the whole app's preferences to the disk
     *
     * @param prefs the preferences to write
     * @param prefsFilePath the json file path to write to
     */
    static writeAppDataPreferencesToDisk(prefs: model.AppPreferences, prefsFilePath: string) {
        const directory = path.dirname(prefsFilePath)

        mkdir(directory, { recursive: true }, () => {
            const json = JSON.stringify(prefs, null, 4)
            writeFileSync(prefsFilePath, json, { encoding: 'utf-8', flag: 'w' })
        })
    }

    // ---------------------------- Parsing ----------------------------//

    private static parseAppPrefsFromJson(json: any): model.AppPreferences {
        const firebaseServiceData = this.parseFirbaseServiceConfigFromJson(
            json[model.FIREBASE_IP_SERVICE_CONFIGURATION] ?? {}
        )
        const detectionServerData = this.parseDetectionServiceConfigFromJson(
            json[model.DETECTION_SERVER_CONFIGURATION] ?? {}
        )
        const serverInformationData = this.parseServerInformation(json[model.SERVER_INFORMATION] ?? {})
        const serverConifgData = this.parseServerConfigFromJson(json[model.SERVER_CONFIGURATION] ?? {})
        const appBehaviorPrefs = this.parseAppBehvaiorPrefs(json[model.APP_BEHAVIOR_PREFS] ?? {})
        const uiPrefs = this.parseUiPrefsFromJson(json[model.UI_PREFS] ?? {})

        return {
            [model.FIREBASE_IP_SERVICE_CONFIGURATION]: firebaseServiceData,
            [model.DETECTION_SERVER_CONFIGURATION]: detectionServerData,
            [model.SERVER_INFORMATION]: serverInformationData,
            [model.SERVER_CONFIGURATION]: serverConifgData,
            [model.UI_PREFS]: uiPrefs,
            [model.APP_BEHAVIOR_PREFS]: appBehaviorPrefs,
        }
    }

    private static parseUiPrefsFromJson(json: any): model.UiPreferences {
        const theme = json[model.THEME] ?? 'system'

        return { theme }
    }

    private static parseAppBehvaiorPrefs(json: any): model.AppBehaviorPrefs {
        const startOnLogin = json[model.START_ON_LOGIN] ?? true
        const runServerOnStartup = json[model.RUN_SERVER_ON_STARTUP] ?? true

        return { startOnLogin, runServerOnStartup }
    }

    private static parseFirbaseServiceConfigFromJson(json: any): model.FirebaseIPServiceConfiguration {
        const globalPort: number = json[model.GLOBAL_PORT] ?? 1919

        return { globalPort, syncIpWithFirebase: true }
    }

    private static parseDetectionServiceConfigFromJson(json: any): model.DetectionServerConfiguration {
        const port: number = json[model.PORT] ?? 4285

        return { port }
    }

    private static parseServerInformation(json: any): model.ServerInformation {
        const uuid: string = json[model.UUID] ?? randomUUID()
        const name: string = json[model.NAME] ?? os.hostname()

        return { name, uuid }
    }

    private static parseServerConfigFromJson(json: any): model.ServerConfiguration {
        const port: number = json[model.PORT] ?? 6543

        return { port }
    }

    updateKey = (group: string, key: string, value: any) => {
        const appPrefs = structuredClone(this.currentPreferences.value)

        const groupObject = appPrefs[group]
        if (groupObject != undefined) {
            groupObject[key] = value
            this.currentPreferences.next(appPrefs)
            PreferencesManager.writeAppDataPreferencesToDisk(appPrefs, this.prefsFilePath)
            return
        }

        console.error(`Invalid Group Or Key ${group} : ${key} `)
    }
}
