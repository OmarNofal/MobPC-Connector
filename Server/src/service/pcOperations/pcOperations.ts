import { exec } from 'child_process'
import notifier from 'node-notifier'
import os from 'os'
import { UnsupportedOperationException } from './exceptions'


/**Represents a notification object to be displayed to the user */
export type Notification = {
    title: string,
    text: string,
    iconPath?: string,
    appName: string
}

/**
 * Containing methods to operate on the PC, like shutting down, locking, etc..
 */
type PCOperations = {
    /**Lock PC (currently available on Windows only) */
    lockPc(): void,

    /**Shutdowns PC immediatly */
    shutdownPc(): void,

    /**Sends a notification
     * @param Notification - the notification to be displayed
     */
    sendNotification(notification: Notification): void
}


// windows-only
function lockPc() {
    if (os.platform() != 'win32') {
        throw new UnsupportedOperationException("Locking is only available on Windows currently")
    }

    exec('rundll32.exe user32.dll,LockWorkStation')
}


function shutdownPc() {
    const platform = os.platform()
    if (platform == 'win32') {
        exec('shutdown /p', (err, stdout, stderr) => { console.log(stderr)})
    } else {
        exec('shutdown now')
    }
}


function sendNotification(notification: Notification) {
    const message = {
        title: notification.title,
        message: notification.text,
        icon: notification.iconPath,
        appID: notification.appName
    }

    notifier.notify(message)
}


export const pcOps: PCOperations = {
    lockPc: lockPc,
    shutdownPc: shutdownPc,
    sendNotification: sendNotification
}