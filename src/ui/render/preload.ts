import { contextBridge, ipcRenderer } from "electron";


contextBridge.exposeInMainWorld(
    'state', 
    {
        'isOn': true,
        'toggle': () => ipcRenderer.send('toggle-server'),
        'observeSubject': (name: string, callback: (_) => void) => {
            const subscription = (_, value) => { callback(value) }   
            ipcRenderer.on(name, subscription)
            return () => { ipcRenderer.removeListener(name, subscription) }
        }
    }
)
