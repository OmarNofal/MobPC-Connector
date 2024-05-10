import AuthorizationManager from '../../auth/auth'
import credentialsManager from '../../credentials/CredentialsManager'
import PreferencesManager from '../../preferences/PreferencesManager'
import { NetworkInterface } from '../../utilities/networkInterfaces'
import os from 'node:os'

/**
 * The payload stored in the QR Code
 * to enable the client device to pair to this server
 */
export type PairingPayload = {
    name: string
    uuid: string
    port: number
    ipAddresses: string[]
    os: string
    cert: string
    /**The token the client device will use to perfrom the pairing process */
    pairingToken: string
}

const platform = os.platform()

export default function generatePairingPayload(
    authManager: AuthorizationManager,
    prefsManager: PreferencesManager, 
    interfaces: NetworkInterface[]
): PairingPayload {
    const cert = credentialsManager.getCredentials().cert

    const serverInformation = prefsManager.currentPreferences.value.serverInformation

    const name = serverInformation.name
    const uuid = serverInformation.uuid
    const port = prefsManager.currentPreferences.value.serverPrefs.port

    return {
        name: name,
        uuid: uuid,
        port: Number(port),
        ipAddresses: interfaces.map((v) => v.ipv4),
        os: platform,
        cert: cert,
        pairingToken: authManager.generatePairingToken()
    }
}
