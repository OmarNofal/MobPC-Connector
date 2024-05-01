import { ipcRenderer } from 'electron'

export const STOP_SERVER_COMMAND = 'stop-server'
export const START_SERVER_COMMAND = 'start-server'

export const mainServerBridge = {
    startServer: () => {
        ipcRenderer.send(START_SERVER_COMMAND)
    },

    stopServer: () => {
        ipcRenderer.send(STOP_SERVER_COMMAND)
    },
}

declare global {
    interface Window {
        mainServer: typeof mainServerBridge
    }
}
