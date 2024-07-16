import { notifyOfNewClipboardItem } from '../utilities/notificationSystem'
import { clipboard } from 'electron'

interface ClipboardService {
    writeTextToClipboard(text: string)
}

function writeTextToClipboard(text: string) {
    clipboard.writeText(text)
    notifyOfNewClipboardItem(text)
}

export const clipboardService: ClipboardService = {
    writeTextToClipboard: writeTextToClipboard,
}
