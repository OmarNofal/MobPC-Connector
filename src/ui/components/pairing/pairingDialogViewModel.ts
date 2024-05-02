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
          qrBase64: string
      }

/**
 * Viewmodel managing the state
 * of the whole pairing process.
 */
export default class PairingViewModel {
    state: BehaviorSubject<PairingState>

    constructor() {
        this.state = new BehaviorSubject({ state: 'loading' })
        this.generateQRCode().then(async (payload) => {
            const qrImage = await QrCode.toDataURL(payload)
            console.log(qrImage)
            const newState: PairingState = {
                state: 'scanning',
                qrBase64: qrImage,
            }
            this.state.next(newState)
        })
    }

    generateQRCode = window.authManager.generatePairingPayload
}
