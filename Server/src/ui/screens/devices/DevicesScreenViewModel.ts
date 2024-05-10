import { BehaviorSubject, map } from 'rxjs'
import { DevicesScreenState } from './DevicesScreenState'
import { MainProcessObservableConnection, subscribeToObservableInMainProcess } from '../../utils'
import { Device, DevicesDB } from '../../../model/device'

export default class DevicesScreenViewModel {
    state: BehaviorSubject<DevicesScreenState>

    private ipcConnection: MainProcessObservableConnection<DevicesDB>

    constructor() {
        this.state = new BehaviorSubject<DevicesScreenState>({ state: 'loading' })
        this.setupObservables()
    }

    deleteDevice = (deviceId: string) => {
        window.authManager.deleteDevice(deviceId)
    }

    private setupObservables = () => {
        this.ipcConnection = subscribeToObservableInMainProcess<DevicesDB>('devices-db-state')

        this.ipcConnection.observable
            .pipe(
                map((devicesDB): DevicesScreenState => {
                    console.log('Devices: ' + devicesDB)
                    const devicesArray: Device[] = []
                    devicesArray.push(...Object.values(devicesDB))
                    return {
                        state: 'loaded',
                        devices: devicesArray,
                    }
                })
            )
            .subscribe(this.state)
    }

    clean = () => {
        this.ipcConnection.clean()
        this.state.unsubscribe()
    }
}
