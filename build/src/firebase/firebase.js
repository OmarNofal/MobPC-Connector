"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const app_1 = require("firebase/app");
const database_1 = require("firebase/database");
const appindentification_1 = require("../identification/appindentification");
const config_json_1 = __importDefault(require("./config.json"));
const ipService_1 = __importDefault(require("./ipService"));
const twentyMinutes = 1000 * 60 * 20;
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
class FirebaseIPService {
    constructor(firebaseServiceConfiguration) {
        this.initDB = () => {
            let app = (0, app_1.initializeApp)(config_json_1.default);
            this.db = (0, database_1.getDatabase)(app);
        };
        /**Starts syncing the global ip address to firebase every `interval` milliseconds */
        this.startService = (interval = twentyMinutes) => {
            if (!this.db)
                this.initDB();
            this.runningInterval = setInterval(this.firebaseRoutine, interval, true);
            this.firebaseRoutine(); // do initial one at the beginning
        };
        /**
         * Stops synchronizing the global ip to firebase.
         * Can be restarted using `startService`
         */
        this.stopService = () => {
            if (!this.runningInterval)
                return;
            clearInterval(this.runningInterval);
            this.runningInterval = undefined;
        };
        this.firebaseRoutine = () => {
            (0, ipService_1.default)((ip) => {
                const db = this.db;
                if (!db) {
                    console.error("Firebase DB closed");
                    this.stopService();
                    return;
                }
                const uuid = (0, appindentification_1.getUUID)();
                const locationRef = (0, database_1.ref)(db, uuid);
                (0, database_1.set)(locationRef, {
                    ip: ip,
                    port: this.serviceConfiguration.value.globalPort
                });
            }, () => {
                console.error("Failed to sync ip to firebase");
            });
        };
        this.serviceConfiguration = firebaseServiceConfiguration;
    }
}
exports.default = FirebaseIPService;
