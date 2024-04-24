"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.notifyOfNewClipboardItem = void 0;
const node_notifier_1 = __importDefault(require("node-notifier"));
function notifyOfNewClipboardItem(text) {
    const message = {
        title: "Copied to clipboard",
        message: text,
        icon: "static/icons/clipboard.png",
        appID: "PC Connector"
    };
    node_notifier_1.default.notify(message);
}
exports.notifyOfNewClipboardItem = notifyOfNewClipboardItem;
