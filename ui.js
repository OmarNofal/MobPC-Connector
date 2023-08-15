const { ipcMain, BrowserWindow, app } = require('electron')




function initWindow() {
    const window = new BrowserWindow({
        width: 1280,
        height: 720,
    });

    window.loadFile("ui/render/index.html");
}


app.whenReady()
.then(
    () => {
        initWindow();
    }
)

app.on('window-all-closed', () => { app.quit() });