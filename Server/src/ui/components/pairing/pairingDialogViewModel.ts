import { BehaviorSubject } from 'rxjs'
import QrCode from 'qrcode'

type PairingState =
    | {
          state: 'loading'
      }
    | {
          state: 'scanning'
          qrBase64: string
      }
    | {
          state: 'done'
      }

/**
 * Viewmodel managing the state
 * of the whole pairing process.
 */
export default class PairingViewModel {
    state: BehaviorSubject<PairingState>

    constructor() {
        this.state = new BehaviorSubject({ state: 'loading' })

        window.authManager.onDeviceConnected(this.onDeviceConnected)

        this.generateQRCode().then(async (payload) => {
            const qrImage = await QrCode.toDataURL(payload.toString())
            console.log(qrImage)
            const newState: PairingState = {
                state: 'scanning',
                qrBase64: qrImage,
            }
            this.state.next(newState)
        })
    }

    // used to close the dialog
    onDeviceConnected = () => {
        this.state.next({ state: 'done' })
    }

    generateQRCode = window.authManager.generatePairingPayload
}
