"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const { ipcRenderer, contextBridge } = require('electron');
contextBridge.exposeInMainWorld('toggleServer', () => {
    return ipcRenderer.invoke('toggle-server');
});
contextBridge.exposeInMainWorld('serverCallback', (callback) => {
    ipcRenderer.on('server-state', callback);
});
