import dgram from 'node:dgram'
import os from 'os'
import { BehaviorSubject, Subscription, distinctUntilKeyChanged } from 'rxjs'
import pkg from '../../package.json'
import { DetectionServerConfiguration, ServerInformation } from '../model/preferences'
import { DetectionServerState } from '../model/detectionServerState'

/**
 * A UDP server which allows client devices to discover
 * this PC. A client sends a UDP broadcast message on the LAN to the server's port
 * and the server will respond with information about the device like
 * its name, IP address, port and the app's version.
 */
export default class DetectionServer {
    /**The socket server itself which listens for broadcast messages */
    private socket: dgram.Socket

    /**The current configuration of the server */
    private currentConfiguration: DetectionServerConfiguration

    /**The subscription to the servcer configuration Subject */
    private subscription: Subscription

    /**The current state of the server */
    state: BehaviorSubject<DetectionServerState>

    serverInformation: BehaviorSubject<ServerInformation>

    /**
     * @param config A behavior subject containing the current configuration of the server
     */
    constructor(config: BehaviorSubject<DetectionServerConfiguration>, serverInformation: BehaviorSubject<ServerInformation>) {
        this.state = new BehaviorSubject<DetectionServerState>(DetectionServerState.STOPPED)
        this.socket = dgram.createSocket('udp4')

        this.serverInformation = serverInformation
        
        this.currentConfiguration = config.value
        this.subscription = config
            .asObservable()
            .pipe(distinctUntilKeyChanged('port'))
            .subscribe((val) => (this.currentConfiguration = val))
    }

    /**
     * Start running the detection server, the port will be determined based on
     * the current value of the configuration passed to the class
     */
    run = () => {
        if (this.state.value == DetectionServerState.RUNNING) {
            console.log('Detection Server already running')
        } else {
            this.socket = dgram.createSocket({ type: 'udp4', reuseAddr: true })
            this.socket.bind(this.currentConfiguration.port, '0.0.0.0', this.emitServerStarted)
            this.socket.on('message', this.onMessageReceived)
        }
    }

    /**Close the ongoing running socket if any */
    close = () => {
        this.socket.close(this.emitServerClosed)
    }

    private emitServerClosed = () => {
        console.log('Detection server closed')
        this.state.next(DetectionServerState.STOPPED)
    }

    private emitServerStarted = () => {
        console.log('Detection server started')
        this.state.next(DetectionServerState.RUNNING)
    }

    private onMessageReceived = (msg: Buffer, remoteInfo: dgram.RemoteInfo) => {
        console.log('Received ' + msg)
        if (msg.toString() === 'PC Connector Discovery') {
            // respond with server info
            const data = {
                name: this.serverInformation.value.name,
                version: pkg.version,
                port: 6543,
                ip: this.socket.address().address,
                id: this.serverInformation.value.uuid,
                os: os.platform(),
            }

            this.socket.send(JSON.stringify(data), remoteInfo.port, remoteInfo.address, (error, _) => {
                if (error != null) {
                    console.log(`Device ${remoteInfo.address} discovered`)
                }
            })
        }
    }

    /**Closes the socket and frees all resources. Can't be run again! */
    clean = () => {
        this.close()
        this.subscription.unsubscribe()
    }
}
