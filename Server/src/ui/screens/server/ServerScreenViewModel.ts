import { BehaviorSubject, Observable, Subject, combineLatest, map } from 'rxjs'
import { MainServerState, MainServerInitialized } from '../../../model/mainServerState'
import { NetworkInterface } from '../../../utilities/networkInterfaces'
import { MainProcessObservableConnection, subscribeToObservableInMainProcess } from '../../utils'

type Loading = 'loading'

export type ServerScreenInitializedState = {
    isRunning: boolean
    name: string
    httpsPort: number
    httpPort: number

    networkInterfaces: NetworkInterface[]
}

export type ServerScreenState = ServerScreenInitializedState | Loading

/**
 * The videmodel
 */
export class ServerScreenViewModel {
    readonly state: BehaviorSubject<ServerScreenState>

    /**
     * The observable of the IPC event
     */
    private mainObservableConnection: MainProcessObservableConnection<any>

    private netInterfacesObservableConnetion: MainProcessObservableConnection<NetworkInterface[]>

    constructor() {
        this.state = new BehaviorSubject('loading')

        this.netInterfacesObservableConnetion = subscribeToObservableInMainProcess('network-interfaces-state')

        let connection = subscribeToObservableInMainProcess<MainServerState>('main-server-state')
        const mainStateObservable = connection.observable.pipe(
            map((value: MainServerState): ServerScreenState => {
                console.log('new event: ' + value)
                if (value == 'init') return 'loading'
                if (value.state == 'closed')
                    return {
                        isRunning: false,
                        httpPort: value.httpPort,
                        httpsPort: value.httpsPort,
                        networkInterfaces: [],
                        name: value.serverName,
                    }
                else {
                    return {
                        isRunning: true,
                        httpPort: value.httpPort,
                        httpsPort: value.httpsPort,
                        networkInterfaces: [],
                        name: value.serverName,
                    }
                }
            })
        )

        combineLatest([mainStateObservable, this.netInterfacesObservableConnetion.observable])
            .pipe(
                map(([serverScreenState, networkInterface]) => {
                    if (serverScreenState == 'loading') {
                        return 'loading'
                    } else {
                        const newState = structuredClone(serverScreenState)
                        newState.networkInterfaces = networkInterface
                        return newState
                    }
                })
            )
            .subscribe(this.state)

        this.mainObservableConnection = connection
    } 

    toggleServer = () => {
        const isRunning = this.state.value != 'loading' && this.state.value.isRunning

        if (!isRunning) {
            window.mainServer.startServer()
        } else {
            window.mainServer.stopServer()
        }
    }

    clean = () => {
        this.state.unsubscribe()
        this.mainObservableConnection.clean()
        this.netInterfacesObservableConnetion.clean()
    }
}
