"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.DetectionServerState = void 0;
const node_dgram_1 = __importDefault(require("node:dgram"));
const os_1 = __importDefault(require("os"));
const rxjs_1 = require("rxjs");
const package_json_1 = __importDefault(require("../package.json"));
const storage_1 = require("./storage");
var DetectionServerState;
(function (DetectionServerState) {
    DetectionServerState[DetectionServerState["RUNNING"] = 0] = "RUNNING";
    DetectionServerState[DetectionServerState["STOPPED"] = 1] = "STOPPED";
})(DetectionServerState || (exports.DetectionServerState = DetectionServerState = {}));
/**
 * A UDP server which allows client devices to discover
 * this PC. A client sends a UDP broadcast message on the LAN to the server's port
 * and the server will respond with information about the device like
 * its name, IP address, port and the app's version.
 */
class DetectionServer {
    /**
     * @param config A behavior subject containing the current configuration of the server
     */
    constructor(config) {
        /**Start running the detection server, the port will be determined based on
         * the current value of the configuration passed to the class
        */
        this.run = () => {
            if (this.state.value == DetectionServerState.RUNNING) {
                console.log("Detection Server already running");
            }
            else {
                this.socket = node_dgram_1.default.createSocket('udp4');
                this.socket.bind(this.currentConfiguration.portNumber, '0.0.0.0', this.emitServerStarted);
                this.socket.on('message', this.onMessageReceived);
            }
        };
        /**Close the ongoing running socket if any */
        this.close = () => {
            this.socket.close(this.emitServerClosed);
        };
        this.emitServerClosed = () => {
            console.log("Detection server closed");
            this.state.next(DetectionServerState.STOPPED);
        };
        this.emitServerStarted = () => {
            console.log("Detection server started");
            this.state.next(DetectionServerState.RUNNING);
        };
        this.onMessageReceived = (msg, remoteInfo) => {
            console.log("Received " + msg);
            if (msg.toString() === 'PC Connector Discovery') {
                // respond with server info
                const data = {
                    name: package_json_1.default.serverName,
                    version: package_json_1.default.version,
                    port: 6543,
                    ip: this.socket.address().address,
                    id: (0, storage_1.getUUID)(),
                    os: os_1.default.platform()
                };
                this.socket.send(JSON.stringify(data), remoteInfo.port, remoteInfo.address, (error, _) => {
                    if (error != null) {
                        console.log(`Device ${remoteInfo.address} discovered`);
                    }
                });
            }
        };
        /**Closes the socket and frees all resources. Can't be run again! */
        this.clean = () => {
            this.close();
            this.subscription.unsubscribe();
        };
        this.state = new rxjs_1.BehaviorSubject(DetectionServerState.STOPPED);
        this.socket = node_dgram_1.default.createSocket('udp4');
        this.currentConfiguration = config.value;
        this.subscription = config.asObservable()
            .pipe((0, rxjs_1.distinctUntilKeyChanged)("portNumber"))
            .subscribe((val) => this.currentConfiguration = val);
    }
}
exports.default = DetectionServer;
