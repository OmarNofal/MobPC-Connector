import { BrowserWindow, app, ipcMain } from 'electron';
import path from 'path';
import { events, isServerOpen, startServer, stopServer } from './app';
import storage from './storage';

function initWindow() {

    const window = new BrowserWindow({
        width: 1280,
        height: 720,
        webPreferences: {
            preload: path.join(__dirname, "ui/render/preload.js")
        }
    });

    events.on('server-started', () => {
        window.webContents.send('server-state', 1);
    })

    
    events.on('server-closed', () => {
        window.webContents.send('server-state', 0);
    })

    window.loadFile("ui/render/index.html");
}



ipcMain.handle('toggle-server', () => {

    console.log("User wants to toggle the server");

    if (!isServerOpen())
        startServer();
    else 
        stopServer();

    return 10
});

app.whenReady().then(
    () => {    
        storage.init(); 
        //storage.changePassword('00000023');
        initWindow();
    });

app.on('window-all-closed', () => { });//app.quit() });