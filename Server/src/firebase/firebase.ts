import { initializeApp } from 'firebase/app'
import { Database, getDatabase, ref, set } from 'firebase/database'
import { BehaviorSubject } from 'rxjs'
import { ServerInformation } from '../model/preferences'
import firebaseConfig from './config.json'
import getIp from './ipService'

const twentyMinutes = 1000 * 60 * 20

export type FirebaseServiceConfiguration = {
    globalPort: number
}

/**
 * A service that syncs this device's global ip address
 * to a database hosted on firebase.
 *
 * This database allows clients devices to find the global ip address and port
 * of a device to be able to connect to it from the WAN.
 *
 * Note: The port exposed on firebase should be forwarded by the router
 * to the real server port using port forwarding.
 */
export default class FirebaseIPService {
    /**The firebase database instance  */
    private db?: Database

    /**The interval that is executing the routine */
    private runningInterval?: number

    /**Current configuration of the service */
    private serviceConfiguration: BehaviorSubject<FirebaseServiceConfiguration>

    private serverInformation: BehaviorSubject<ServerInformation>

    constructor(
        firebaseServiceConfiguration: BehaviorSubject<FirebaseServiceConfiguration>,
        serverInformation: BehaviorSubject<ServerInformation>
    ) {
        this.serviceConfiguration = firebaseServiceConfiguration
        this.serverInformation = serverInformation
    }

    private initDB = () => {
        let app = initializeApp(firebaseConfig)
        this.db = getDatabase(app)
    }

    /**Starts syncing the global ip address to firebase every `interval` milliseconds */
    startService = (interval: number = twentyMinutes) => {
        if (!this.db) this.initDB()
        this.runningInterval = setInterval(this.firebaseRoutine, interval, true)
        this.firebaseRoutine() // do initial one at the beginning
    }

    /**
     * Stops synchronizing the global ip to firebase.
     * Can be restarted using `startService`
     */
    stopService = () => {
        if (!this.runningInterval) return
        clearInterval(this.runningInterval)
        this.runningInterval = undefined
    }

    private firebaseRoutine = () => {
        getIp(
            (ip: string) => {
                const db = this.db
                if (!db) {
                    console.error('Firebase DB closed')
                    this.stopService()
                    return
                }
                const uuid = this.serverInformation.value.uuid
                const locationRef = ref(db, uuid)
                set(locationRef, {
                    ip: ip,
                    port: this.serviceConfiguration.value.globalPort,
                })
            },
            () => {
                console.error('Failed to sync ip to firebase')
            }
        )
    }
}
