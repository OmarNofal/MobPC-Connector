const lockYourWindows = require('lock-your-windows');
const os = require('os');
const {UnsupportedOperationException} = require('./exceptions');
const {exec} = require('child_process');
const notifier = require('node-notifier');
// Operations done on the OS Level like locking and shutting down




function lockPc() {
    if (os.platform() != 'win32') {
        throw new UnsupportedOperationException("Locking is only available on Windows currently");
    }

    lockYourWindows.lock();
}


function shutdownPc() {
    const platform = os.platform();
    if (platform == 'win32') {
        exec('shutdown /p', (err, stdout, stderr) => { console.log(stderr)});
    } else {
        exec('shutdown now');
    }
}


function sendNotification(appName, title, body, iconPath = undefined) {
    const message = {
        title: title,
        message: body,
        icon: iconPath,
        appID: appName
    }

    notifier.notify(message)
}


module.exports = {lockPc, shutdownPc, sendNotification};