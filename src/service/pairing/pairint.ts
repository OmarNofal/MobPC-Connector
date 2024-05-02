import credentialsManager from "../../credentials/CredentialsManager";
import PreferencesManager from "../../preferences/PreferencesManager";
import { NetworkInterface } from "../../utilities/networkInterfaces";



export type PairingPayload = {
    name: string
    uuid: string
    port: number
    ipAddresses: string[]
    cert: string
}



export default function generatePairingPayload(
    prefsManager: PreferencesManager,
    interfaces: NetworkInterface[]
) {

    const cert = credentialsManager.getCredentials().cert

    const serverInformation = prefsManager.currentPreferences.value.serverInformation
    
    const name = serverInformation.name
    const uuid = serverInformation.uuid
    const port = prefsManager.currentPreferences.value.serverPrefs.port


    return {
        name: name,
        uuid: uuid,
        port: port,
        cert: cert,
        ipAddresses: interfaces.map(v => v.ipv4)
    }
}