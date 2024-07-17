import { useEffect, useState } from 'react'

export default function useTimedStateChange(
    initial: string,
    delayMs: number,
    onTimeFinished: (newValue: string) => void
): [string, (_: string) => void] {
    const [text, setText] = useState(initial)
    const [oldText, setOldText] = useState(initial)

    useEffect(() => {
        const timerId = setTimeout(() => {
            if (text != oldText) {
                onTimeFinished(text)
                setOldText(text)
            }
        }, delayMs)

        return function cancel() {
            clearTimeout(timerId)
        }
    }, [text])

    return [text, setText]
}
