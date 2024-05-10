import fs from 'fs';
import { IncomingMessage } from 'http';
import { Duplex } from 'stream';
import { RawData, default as WebSocket, default as ws } from 'ws';
import { parsePath } from '../fs/operations';
import crypto from 'crypto'

/**
 * Represents a single connection to the `FileSystemWatcherService`
 */
type FSWatcherConnection = {
    /**The socket associated with the connection */
    socket: WebSocket,

    /**The id of the connection */
    id: string,

    /**The current fs watcher that is notifying the user if any */
    watcher?: fs.FSWatcher
}

type Connections = Map<string, FSWatcherConnection>
/**
 * Provides a way to alert client devices using `WebSockets`
 * that a particular directory or a file has been modified or deleted
 * 
 * This is used by client devices to show directory updates in real-time
 */
export default class FileSystemWatcherService {

    /**The websocket server */
    private server: WebSocket.Server

    /**The connections */
    private connections: Connections
        = new Map<string, FSWatcherConnection>() 

    constructor() {
        this.server = new ws.Server({noServer: true})
        this.server.on('connection', this.handleNewConnection)
    }

    /**
     * Upgrades an http connection to a WebSocket connection and
     * then adds it to the list of connections to the server
     */
    handleIncomingConnection = (req: IncomingMessage, socket: Duplex, head: Buffer) => {
        this.server.handleUpgrade(req, socket, head, newWebSocket => {
            this.handleNewConnection(newWebSocket)
        });
    }

    /**
     * Handles a new `WebSocket` connection to the 
     * `FileSystemWatcherService`
     */
    private handleNewConnection = (socket: WebSocket) => {

        const connectionId = crypto.randomUUID()
    
        let connection: FSWatcherConnection = {
            socket: socket,
            id: connectionId
        }
        this.connections.set(connectionId, connection)

        socket.on(
            'message', 
            message => this.handleNewMessage(connectionId, message)
        )
      
        socket.on('close', () => this.handleClose(connectionId))
        socket.on('error', () => this.handleError(connectionId))

    }

    private handleNewMessage = (connId: string, message: RawData) => {
        let connection = this.connections.get(connId)
        if (!connection) return // this shouldn't happen
        
        const resourcePath: string = parsePath(message.toString())

        console.log("User wants to watch " + resourcePath);
      
        if (connection.watcher != undefined)
            connection.watcher.close();
    
        try {
            connection.watcher = fs.watch(resourcePath);
        } catch (e) {
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
    }

    
    private handleError = (connId: string) => {
        let connection = this.connections.get(connId)
        if (!connection) return
        
        this.connections.delete(connId)
        connection.socket.close()
        connection.watcher?.close()
    }

    private handleClose = (connId: string) => {
        let connection = this.connections.get(connId)
        if (!connection) return
        
        this.connections.delete(connId)
        connection.socket.close()
        connection.watcher?.close()
    }
}
