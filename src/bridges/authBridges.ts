import { ipcRenderer } from 'electron'
import { PairingPayload } from '../service/pairing/pairing'

export const GENERATE_PAIRING_PAYLOAD = 'generate-pairing-payload'
export const DELETE_DEVICE = 'delete-device'

export const authBridge = {
    generatePairingPayload: (): Promise<PairingPayload> => ipcRenderer.invoke(GENERATE_PAIRING_PAYLOAD),
    deleteDevice: (deviceId: string) => ipcRenderer.invoke(DELETE_DEVICE, deviceId)
}