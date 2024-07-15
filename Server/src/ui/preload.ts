import { contextBridge, ipcRenderer } from 'electron'
import { mainServerBridge } from '../bridges/mainServerBridge'
import { authBridge } from '../bridges/authBridges'
import { preferencesBridge } from '../bridges/preferencesBridge'

contextBridge.exposeInMainWorld('authManager', authBridge)

contextBridge.exposeInMainWorld('mainServer', mainServerBridge)

contextBridge.exposeInMainWorld('prefs', preferencesBridge)

const stateBridge = {
    /**
     *
     * Register to receive new values of
     * an observable on the main process.
     *
     *
     * @param name The name of the observable
     * @param callback The callback to execute for new values
     * @returns callback to unregister
     */
    observeSubject: (name: string, callback: (_: any) => void) => {
        const subscription = (_, value: any) => {
            callback(value)
        }
        ipcRenderer.on(name, subscription)
        // here we are asking the main process to send
        // the latest value of the observable
        // if it is a behaviour subject
        ipcRenderer.send('send-subject-latest-value', name)
        return () => {
            ipcRenderer.removeListener(name, subscription)
        }
    },
}

contextBridge.exposeInMainWorld('state', stateBridge)

declare global {
    interface Window {
        state: typeof stateBridge
        mainServer: typeof mainServerBridge
        authManager: typeof authBridge
        prefs: typeof preferencesBridge
    }
}
