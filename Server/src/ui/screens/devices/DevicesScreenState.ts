import { Device } from '../../../model/device'

export type DevicesScreenState =
    | {
          state: 'loading'
      }
    | {
          state: 'loaded'
          devices: Device[]
      }
