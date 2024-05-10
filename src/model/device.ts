/**
 * Represents a device currently paired
 * with this server.
 *
 * Note: When a device is paired, a unique id is generated
 * , and it doesn't matter if the same device paired once before, as it will
 * get assigned a new id and a new token
 */
export type Device = {
    /** A unique id identifying the device */
    id: string

    /** The OS of the device */
    os: string

    /** The name of the device model */
    modelName: string

    /** The date when the device was paired with the server */
    pairingDate: Date
}


export type DevicesDB = {
    [deviceId: string]: Device
}
