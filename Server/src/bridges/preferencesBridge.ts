
import { ipcRenderer } from 'electron'
import {AppPreferences} from '../model/preferences'
import { IPC_UPDATE_SETTINGS } from '../ipc/ipcHandlers'



export const preferencesBridge = {

    updatePreferencesKey: (group: keyof AppPreferences, key: string, value: any) => ipcRenderer.invoke(IPC_UPDATE_SETTINGS, group, key, value),

}