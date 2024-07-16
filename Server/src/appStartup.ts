import { app } from 'electron'

export function setAppStartOnLogin(shouldStartOnLogin: boolean) {
    app.setLoginItemSettings({
        enabled: shouldStartOnLogin,
        openAtLogin: shouldStartOnLogin,
        args: ['--login'], // flag to know that we launched from logging in (used to prevent window from showing)
    })
}
