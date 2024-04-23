"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const electron_1 = require("electron");
const path_1 = __importDefault(require("path"));
const app_1 = require("./app");
const storage_1 = __importDefault(require("./storage"));
function initWindow() {
    const window = new electron_1.BrowserWindow({
        width: 1280,
        height: 720,
        webPreferences: {
            preload: path_1.default.join(__dirname, "ui/render/preload.js")
        }
    });
    app_1.events.on('server-started', () => {
        window.webContents.send('server-state', 1);
    });
    app_1.events.on('server-closed', () => {
        window.webContents.send('server-state', 0);
    });
    window.loadFile("ui/render/index.html");
}
electron_1.ipcMain.handle('toggle-server', () => {
    console.log("User wants to toggle the server");
    if (!(0, app_1.isServerOpen)())
        (0, app_1.startServer)();
    else
        (0, app_1.stopServer)();
    return 10;
});
electron_1.app.whenReady().then(() => {
    storage_1.default.init();
    //storage.changePassword('00000023');
    initWindow();
});
electron_1.app.on('window-all-closed', () => { }); //app.quit() });
