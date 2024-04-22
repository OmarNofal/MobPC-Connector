const { ipcMain, BrowserWindow, app } = require('electron')
const path = require('path');
const { startServer, stopServer, events, isServerOpen } = require('./app');
const { start } = require('repl');
const storage = require('./storage');

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
        initWindow();
    });

app.on('window-all-closed', () => { app.quit() });