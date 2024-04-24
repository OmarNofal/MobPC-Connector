"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.pcOps = void 0;
const child_process_1 = require("child_process");
const lock_your_windows_1 = __importDefault(require("lock-your-windows"));
const node_notifier_1 = __importDefault(require("node-notifier"));
const os_1 = __importDefault(require("os"));
const exceptions_1 = require("./exceptions");
function lockPc() {
    if (os_1.default.platform() != 'win32') {
        throw new exceptions_1.UnsupportedOperationException("Locking is only available on Windows currently");
    }
    lock_your_windows_1.default.lock();
}
function shutdownPc() {
    const platform = os_1.default.platform();
    if (platform == 'win32') {
        (0, child_process_1.exec)('shutdown /p', (err, stdout, stderr) => { console.log(stderr); });
    }
    else {
        (0, child_process_1.exec)('shutdown now');
    }
}
function sendNotification(notification) {
    const message = {
        title: notification.title,
        message: notification.text,
        icon: notification.iconPath,
        appID: notification.appName
    };
    node_notifier_1.default.notify(message);
}
exports.pcOps = {
    lockPc: lockPc,
    shutdownPc: shutdownPc,
    sendNotification: sendNotification
};
