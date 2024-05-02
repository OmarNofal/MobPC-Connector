import { useState, useEffect } from 'react'
import { BehaviorSubject, Observable } from 'rxjs'

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
