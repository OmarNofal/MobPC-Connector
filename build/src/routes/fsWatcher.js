"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const ws_1 = __importDefault(require("ws"));
const fs_1 = __importDefault(require("fs"));
const operations_1 = require("../fs/operations");
const server = new ws_1.default.Server({ noServer: true });
server.on('connection', socket => {
    var watcher = null;
    socket.on('message', message => {
        const resource = (0, operations_1.parsePath)(message.toString());
        console.log("User wants to watch " + resource);
        if (watcher != null)
            watcher.close();
        try {
            watcher = fs_1.default.watch(resource);
        }
        catch (e) {
            console.log(e);
            socket.send('unavailable');
        }
        watcher.on('change', (eventType, fileName) => {
            console.log("Directory or file changed");
            socket.send('changed');
        });
        watcher.on('error', error => {
            socket.send('file deleted');
        });
        socket.send('ok');
    });
    socket.on('close', () => {
        watcher.close();
    });
    socket.on('error', () => {
        watcher.close();
    });
});
exports.default = server;
