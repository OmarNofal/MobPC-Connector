/**
 * Groups
 */
export const FIREBASE_IP_SERVICE_CONFIGURATION = 'firebaseServicePrefs'
export const DETECTION_SERVER_CONFIGURATION = 'detectionServerPrefs'
export const SERVER_INFORMATION = 'serverInformation'
export const SERVER_CONFIGURATION = 'serverPrefs'
export const APP_BEHAVIOR_PREFS = 'appBehaviorPrefs'
export const UI_PREFS = 'uiPrefs'

/**Keys */
export const GLOBAL_PORT = 'globalPort'
export const SYNC_IP_WITH_FIREBASE = 'syncIpWithFirebase'

export const PORT = 'port'
export const NAME = 'name'
export const UUID = 'uuid'

export const START_ON_LOGIN = 'startOnLogin'
export const RUN_SERVER_ON_STARTUP = 'runServerOnStartup'

export const THEME = 'theme'

/**
 * Contains configuration for the
 * firebase service
 */
export interface FirebaseIPServiceConfiguration {
    /**
     * The port used to access the server from outside the local network
     *
     * This port should be forwarded by the router to this server's local IP address and port.
     *
     *  -------------------------------  globalPort  ------  local ip and port    ----------
     * |Device From outside the network| ----->     |Router| ----------------->  |The server|
     *  -------------------------------              ------    Port fowarding     ----------
     */
    [GLOBAL_PORT]: number

    /**
     * Should we sync the IP address with firebase?
     * 
     * In other words, should the service be running or not
     */
    [SYNC_IP_WITH_FIREBASE]: boolean
}

/**
 * Contains configuration for the detection
 * server
 */
export interface DetectionServerConfiguration {
    [PORT]: number
}

/**
 * Contains information about the server
 * like the name and the UUID
 */
export interface ServerInformation {
    /**
     * The name of this server, defaulting to the os hostname.
     */
    [NAME]: string

    /**
     * A unique id identifying this server
     */
    [UUID]: string
}

/**
 * The configuration of the main server that the clients
 * interact with
 */
export interface ServerConfiguration {
    [PORT]: number
}

export interface AppBehaviorPrefs {
    /**
     * Start the app on system login?
     */
    [START_ON_LOGIN]: boolean

    /**
     * If [startOnLogin] is enabled, do we run the server as soon as the app starts?
     */
    [RUN_SERVER_ON_STARTUP]: boolean
}

export type Theme = 'dark' | 'light' | 'system'

export interface UiPreferences {
    /**
     * The theme of the application
     */
    [THEME]: Theme
}

/**
 * This contains all the preferences of the app
 */
export interface AppPreferences {
    [FIREBASE_IP_SERVICE_CONFIGURATION]: FirebaseIPServiceConfiguration
    [DETECTION_SERVER_CONFIGURATION]: DetectionServerConfiguration
    [SERVER_INFORMATION]: ServerInformation
    [SERVER_CONFIGURATION]: ServerConfiguration
    [UI_PREFS]: UiPreferences
    [APP_BEHAVIOR_PREFS]: AppBehaviorPrefs
}
