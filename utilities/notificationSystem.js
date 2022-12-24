const notifier = require('node-notifier');



function notifyOfNewClipboardItem(text) {

    const message = {
        title: "New text in your clipboard",
        message: text,
        icon: "static/icons/clipboard.png",
        appID: "PC Connector"
    }

    notifier.notify(message)

}


module.exports =  { notifyOfNewClipboardItem }