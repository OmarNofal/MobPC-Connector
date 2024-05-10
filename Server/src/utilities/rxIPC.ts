import { Observable, Subscription } from "rxjs";




/**
 * 
 * This function subscribes to an observable on the main process
 * and emits new data through an ipc channel to a browser window
 * 
 * @param observable The observable to pipe to the ipc channel
 * @param webcontents The webcontents of the target browser window
 * @param channelName The channel name to send the data to
 * @returns Subscription, which must be cancelled when no longer needed
 */
export function pipeObservableToIPC<T>(
    observable: Observable<T>,
    webContents: Electron.WebContents,
    channelName: string
): Subscription {
    return observable.subscribe((value) =>
        webContents.send(channelName, value)
    )
}