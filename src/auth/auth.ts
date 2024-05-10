import { randomBytes } from 'crypto'
import fs from 'fs'
import jwt from 'jsonwebtoken'
import path from 'path'
import { BehaviorSubject } from 'rxjs'
import { v4 as uuidv4 } from 'uuid'
import { DevicesDB } from '../model/device'

const secretFileName = 'secret'
const devicesDBFileName = 'devices.json'

/**
 * This class manages routines related to authorization like generating pairing tokens,
 * generating access tokens, checking if a token is valid, etc...
 */
export default class AuthorizationManager {
    /**Directory where the secret is stored */
    secretDirectory: string

    /**
     * Path to the file containing the secret.
     *
     * This should be pointing in private app storage
     */
    secretFilePath: string

    /**
     * The secret itself
     *
     * This is cached mainly to prevent excessive disk I/Os
     * when validating the token for each request
     */
    private secret?: string

    private devicesDBPath: string

    /**
     * A database containing all the currently paired devices
     */
    devicesDatabase: BehaviorSubject<DevicesDB>

    /**@param secretDirectroy Directory where this class fetches and saves passwords */
    constructor(secretDirectroy: string) {
        this.devicesDatabase = new BehaviorSubject({})

        this.secretDirectory = secretDirectroy
        this.secretFilePath = path.join(secretDirectroy, secretFileName)
        this.devicesDBPath = path.join(secretDirectroy, devicesDBFileName)

        this.secret = this.readSecret()
        this.ensureSecretLoaded() // if the secret is not created yet

        this.devicesDatabase.subscribe(console.log)
        this.loadDevicesDB()
    }

    /**
     * Changes the secret, which will implicitly unpair all
     * registered devices and revoke all access tokens.
     */
    generateAndSaveSecret = () => {
        const newSecret = uuidv4({ random: randomBytes(16) })

        this.saveSecret(newSecret)
        this.secret = newSecret
    }

    /**
     * Wheteher we have set a secret or not
     *
     * This will return false when the app is started for the first time.
     *
     * If the secret is not set, then all requests should be rejected untill
     * a secret is set
     */
    isSecretSet = () => {
        return this.secret != undefined
    }

    /**
     * Registers a new device as a paired device
     * and assigns it a new token
     *
     * @param deviceInfo The info of the device
     * @returns a token which should be sent to the device to be
     * used for authorization in subsequent requests
     */
    pairWithNewDevice = (deviceInfo: { os: string; modelName: string }): string => {
        this.ensureSecretLoaded()

        const newDeviceId = uuidv4({ random: randomBytes(16) })
        const pairingDate = new Date()

        const payload = {
            id: newDeviceId,
        }

        const token = jwt.sign(payload, this.secret)

        // save the new device to the database
        const oldDB = this.devicesDatabase.value
        const newDB = structuredClone(oldDB)

        newDB[newDeviceId] = {
            id: newDeviceId,
            os: deviceInfo.os,
            modelName: deviceInfo.modelName,
            pairingDate: pairingDate,
        }
        this.saveDevicesDBToDisk(newDB)

        // update the in-memory database
        this.devicesDatabase.next(newDB)

        return token
    }


    /**
     * Deletes a device from the database
     * effectively logging it out from the server
     * 
     * @param deviceId The device ID to delete
     */
    deleteDevice = (deviceId: string) => {
        
        const oldDB = this.devicesDatabase.value
        const newDB = structuredClone(oldDB)

        delete newDB[deviceId]
        this.devicesDatabase.next(newDB)
        this.saveDevicesDBToDisk(newDB)
    }

    /**
     * Generates a token to be used by device to pair with the server.
     *
     * Valid for 5 minutes.
     * @returns token used for pairing
     */
    generatePairingToken = (): string => {
        return jwt.sign({ pairing_token: true }, this.secret, { expiresIn: 5 * 60 })
    }

    /**
     * Returns true if the token is a valid pairing token
     * @param token the token to verify
     */
    isValidPairingToken = (token: string) => {
        try {
            const payload = jwt.verify(token, this.secret)
            return payload['pairing_token'] === true
        } catch {
            return false
        }
    }

    /**
     * Checks if a token is a valid device token id
     *
     * @returns true if the token is valid and not expired, false otherwise
     */
    isValidDeviceToken = (token: string) => {
        try {
            const payload = jwt.verify(token, this.secret)
            console.log(payload)

            if (payload.hasOwnProperty('id')) {
                // check if it exists in the database
                const deviceId = payload['id']
                const db = this.devicesDatabase.value

                if (db.hasOwnProperty(deviceId)) return true
                console.log('Returned false')
            }

            return false
        } catch (e) {
            // invalid | expired token
            return false
        }
    }

    /**Saves the secret to the filesystem */
    private saveSecret = async (hash: string) => {
        const buffer = Buffer.from(hash, 'utf-8')
        fs.writeFileSync(this.secretFilePath, buffer)
    }

    /**Reads the secret from the system */
    private readSecret = () => {
        try {
            const data = fs.readFileSync(this.secretFilePath)
            return data.toString('utf-8')
        } catch (e: any) {
            return undefined
        }
    }

    /**
     * Saves the devices database to disk
     * @param db The database to save
     */
    private saveDevicesDBToDisk(db: DevicesDB) {
        const buffer = Buffer.from(JSON.stringify(db), 'utf-8')
        fs.writeFileSync(this.devicesDBPath, buffer, { flag: 'w+' })
    }

    /**
     * Loads the Devices Database from the disk into memory
     *
     * This generates an empty database if it does not exist
     */
    private loadDevicesDB = () => {
        try {
            const data = fs.readFileSync(this.devicesDBPath)
            this.devicesDatabase.next(JSON.parse(data.toString('utf-8')) as DevicesDB)
        } catch (e: any) {
            this.devicesDatabase.next({}) // use empty database as fallback
        }
    }

    /**
     * If the secret is loaded, does nothing.
     * If not, reads or creates a new secret.
     */
    private ensureSecretLoaded = () => {
        if (!this.isSecretSet()) {
            this.generateAndSaveSecret()
        }
    }
}
