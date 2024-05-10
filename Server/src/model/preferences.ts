


/**
 * Contains configuration for the 
 * firebase service
 */
export interface FirebaseIPServiceConfiguration {
    globalPort: number
}

/**
 * Contains configuration for the detection
 * server
 */
export interface DetectionServerConfiguration {
    port: number
}


/**
 * Contains information about the server
 * like the name and the UUID
 */
export interface ServerInformation {
    /**
     * The name of this server, defaulting to the os hostname. 
     */
    name: string,

    /**
     * A unique id identifying this server
     */
    uuid: string
}

/**
 * The configuration of the main server that the clients
 * interact with
 */
export interface ServerConfiguration {
    port: string,
}


/**
 * This contains all the preferences of the app
 */
export interface AppPreferences {
    firebaseServicePrefs: FirebaseIPServiceConfiguration,
    detectionServerPrefs: DetectionServerConfiguration,
    serverInformation: ServerInformation,
    serverPrefs: ServerConfiguration
}