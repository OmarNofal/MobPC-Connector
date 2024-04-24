"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs_1 = __importDefault(require("fs"));
const ws_1 = __importDefault(require("ws"));
const operations_1 = require("../fs/operations");
/**
 * Provides a way to alert client devices using `WebSockets`
 * that a particular directory or a file has been modified or deleted
 *
 * This is used by client devices to show directory updates in real-time
 */
class FileSystemWatcherService {
    constructor() {
        /**The connections */
        this.connections = new Map();
        /**
         * Upgrades an http connection to a WebSocket connection and
         * then adds it to the list of connections to the server
         */
        this.handleIncomingConnection = (req, socket, head) => {
            this.server.handleUpgrade(req, socket, head, newWebSocket => {
                this.handleNewConnection(newWebSocket);
            });
        };
        /**
         * Handles a new `WebSocket` connection to the
         * `FileSystemWatcherService`
         */
        this.handleNewConnection = (socket) => {
            const connectionId = crypto.randomUUID();
            let connection = {
                socket: socket,
                id: connectionId
            };
            this.connections.set(connectionId, connection);
            socket.on('message', message => this.handleNewMessage(connectionId, message));
            socket.on('close', () => this.handleClose(connectionId));
            socket.on('error', () => this.handleError(connectionId));
        };
        this.handleNewMessage = (connId, message) => {
            let connection = this.connections.get(connId);
            if (!connection)
                return; // this shouldn't happen
            const resourcePath = (0, operations_1.parsePath)(message.toString());
            console.log("User wants to watch " + resourcePath);
            if (connection.watcher != undefined)
                connection.watcher.close();
            try {
                connection.watcher = fs_1.default.watch(resourcePath);
            }
            catch (e) {
                console.log(e);
                connection.socket.send('unavailable');
            }
            connection.watcher.on('change', (eventType, fileName) => {
                console.log("Directory or file changed");
                connection.socket.send('changed');
            });
            connection.watcher.on('error', error => {
                connection.socket.send('file deleted');
            });
            connection.socket.send('ok');
        };
        this.handleError = (connId) => {
            var _a;
            let connection = this.connections.get(connId);
            if (!connection)
                return;
            this.connections.delete(connId);
            connection.socket.close();
            (_a = connection.watcher) === null || _a === void 0 ? void 0 : _a.close();
        };
        this.handleClose = (connId) => {
            var _a;
            let connection = this.connections.get(connId);
            if (!connection)
                return;
            this.connections.delete(connId);
            connection.socket.close();
            (_a = connection.watcher) === null || _a === void 0 ? void 0 : _a.close();
        };
        this.server = new ws_1.default.Server({ noServer: true });
        this.server.on('connection', this.handleNewConnection);
    }
}
exports.default = FileSystemWatcherService;
