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
        const initalPrefs = PreferencesManager.readAppPreferencesFromDisk(this.prefsFilePath) // should we async this?

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
        console.log(directory)

        mkdir(directory, { recursive: true }, () => {
            const json = JSON.stringify(prefs)
            writeFileSync(prefsFilePath, json, { encoding: 'utf-8', flag: 'w' })
        })
    }

    private static parseAppPrefsFromJson(json: any): model.AppPreferences {
        const firebaseServiceData = this.parseFirbaseServiceConfigFromJson(json.globalIp ?? {})
        const detectionServerData = this.parseDetectionServiceConfigFromJson(json.detectionService ?? {})
        const serverInformationData = this.parseServerInformation(json.serverInformation ?? {})
        const serverConifgData = this.parseServerConfigFromJson(json.serverConfiguration ?? {})

        return {
            firebaseServicePrefs: firebaseServiceData,
            detectionServerPrefs: detectionServerData,
            serverInformation: serverInformationData,
            serverPrefs: serverConifgData,
        }
    }

    private static parseFirbaseServiceConfigFromJson(json: any): model.FirebaseIPServiceConfiguration {
        const globalPort: number = json.globalPort ?? 1919

        return { globalPort: globalPort }
    }

    private static parseDetectionServiceConfigFromJson(json: any): model.DetectionServerConfiguration {
        const port: number = json.port ?? 4285

        return { port: port }
    }

    private static parseServerInformation(json: any): model.ServerInformation {
        const uuid: string = json.uuid ?? randomUUID()
        const name: string = json.name ?? os.hostname()

        return { name: name, uuid: uuid }
    }

    private static parseServerConfigFromJson(json: any): model.ServerConfiguration {
        const port: string = json.port ?? 6543

        return { port: port }
    }
}
