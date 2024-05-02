import { ipcRenderer } from 'electron'
import { PairingPayload } from '../auth/pairingPayload'

export const GENERATE_PAIRING_PAYLOAD = 'generate-pairing-payload'

export const authBridge = {
    generatePairingPayload: (): Promise<PairingPayload> => ipcRenderer.invoke(GENERATE_PAIRING_PAYLOAD),
}