import { BehaviorSubject, Observable, Subject, map } from "rxjs"
import { DetectionServerState } from "../../model/detectionServerState"





type Loading = 'loading'

export type MainScreenState = {
    isMainServerOpen: boolean,
    numberOfDevicesConnected: number 
} | Loading


export class MainScreenViewModel {

    readonly state: BehaviorSubject<MainScreenState> 

    private mainObservableConnection: MainProcessObservableConnection<any>

    constructor() {
        this.state = new BehaviorSubject('loading')
        let observable = subscribeToObservableInMainProcess<DetectionServerState>('detection-server-state')
        observable.observable.pipe(
            map(
                (value: DetectionServerState) => { 
                    console.log('value changed to ' + value)
                    return {
                        isMainServerOpen: value == DetectionServerState.RUNNING, 
                        numberOfDevicesConnected: 34
                    }
                }
            )
        ).subscribe(this.state)
        this.mainObservableConnection = observable
    }

    stop = () => {
        window.state.toggle()
    }

    start = () => {
        this.state.next('loading')
    }

    clean = () => {
        this.state.unsubscribe()
        this.mainObservableConnection.clean()
    }

}





class MainProcessObservableConnection<T> {


    /**
     * The observable that updates 
     * when a new event is received through ipc
     */
    observable: Observable<T>

    /**
     * A function used to unregister the listener
     * attached to the ipcRenderer
     */
    listenerRemover: () => void

    constructor(
        observable: Observable<T>,
        listenerRemover: () => void
    ) {
        this.observable = observable
        this.listenerRemover = listenerRemover        
    }

    clean = () => {
        this.listenerRemover()
    }  
}

function subscribeToObservableInMainProcess<T>(
    name: string,
): MainProcessObservableConnection<T> {
    
    let observable = new Subject<T>()
    let removeListener = window.state.observeSubject(name, (value: T) => observable.next(value)) 

    return new MainProcessObservableConnection(observable, removeListener)
}