import notifier from 'node-notifier'


export function notifyOfNewClipboardItem(text: string) {

    const message = {
        title: "New text in your clipboard",
        message: text,
        icon: "static/icons/clipboard.png",
        appID: "PC Connector"
    }

    notifier.notify(message)
}
