import { app, nativeImage, NativeImage, Notification } from 'electron'

type NotificationPayload = {
    title: string
    text: string
    icon?: Buffer // bitmap byte array
    appName: string
}

interface NotificationService {
    postNotification: (payload: NotificationPayload) => void
}

function postNotification(payload: NotificationPayload) {
    let icon = null
    if (payload.icon) {
        icon = nativeImage.createFromBuffer(payload.icon)
    }

    new Notification({
        title: payload.title,
        body: payload.text,
        subtitle: payload.appName,
        icon: icon
    }).show()
}

export const notificationService: NotificationService = {
    postNotification,
}
