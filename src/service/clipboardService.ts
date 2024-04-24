import clipboardy from 'clipboardy';
import { notifyOfNewClipboardItem } from '../utilities/notificationSystem';


interface ClipboardService {

    writeTextToClipboard(text: string)
}

function writeTextToClipboard(text: string) {
    clipboardy.write(text);
    notifyOfNewClipboardItem(text);
}

export const clipboardService: ClipboardService = {
    writeTextToClipboard: writeTextToClipboard
}