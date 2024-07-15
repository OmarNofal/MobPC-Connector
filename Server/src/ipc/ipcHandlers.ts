import { ipcMain } from 'electron'
import PreferencesManager from '../preferences/PreferencesManager'

export const IPC_UPDATE_SETTINGS = 'prefs:updateSettings'

export function setupIPCMainPrefsHandler(prefsManager: PreferencesManager) {
    ipcMain.handle(IPC_UPDATE_SETTINGS, (_, group, key, value) => prefsManager.updateKey(group, key, value))
}
