import { useState, useEffect } from 'react'
import { BehaviorSubject, Observable, Subject } from 'rxjs'

function get<T>(observable$: Observable<T>): T {
    let value
    observable$.subscribe((val) => (value = val)).unsubscribe()
    return value
}

export function useUnwrap<T>(observable$: BehaviorSubject<T>): T {
    const [value, setValue] = useState(() => get(observable$))

    useEffect(() => {
        const subscription = observable$.subscribe(setValue)
        return function cleanup() {
            subscription.unsubscribe()
        }
    }, [observable$])

    return value
}

export class MainProcessObservableConnection<T> {
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

    constructor(observable: Observable<T>, listenerRemover: () => void) {
        this.observable = observable
        this.listenerRemover = listenerRemover
    }

    clean = () => {
        this.listenerRemover()
    }
}

export function subscribeToObservableInMainProcess<T>(name: string): MainProcessObservableConnection<T> {
    let observable = new Subject<T>()
    let removeListener = window.state.observeSubject(name, (value: T) => observable.next(value))

    return new MainProcessObservableConnection(observable, removeListener)
}
