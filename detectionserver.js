const dgram = require('node:dgram')
const package = require('./package.json')
const { getUUID } = require('./identification/appindentification')
const os = require('os');
// This is a UDP socket server to help other devices discover the server
// Typically, you would send a broadcast message to the whole local network
// to the port 4285 and if the server is there it will respond with the status of the system

SOCKET_SERVER_PORT = 4285
let server = dgram.createSocket('udp4')


server.on('listening', () => {
    console.log("Detection Server is up and running on PORT: " + server.address().port)
})

server.on('message', (msg, remoteInfo) => {
    console.log("Received " + msg)
    if (msg.toString() === 'PC Connector Discovery') {
        // respond with server info
        const data = {
            name: package.serverName,
            version: package.version,
            port: 6543,
            ip: server.address().address,
            id: getUUID(),
            os: os.platform()
        }

        server.send(JSON.stringify(data), remoteInfo.port, remoteInfo.address, (error, _) => {
            if (error != null) {
                console.log(`Device ${remoteInfo.address} discovered`)
            }
        })
    }
})


function closeDetectionServer() {
    server.close();
    server = dgram.createSocket('udp4');
}

function runDetectionServer() {
    server.bind(SOCKET_SERVER_PORT, '0.0.0.0');//"192.168.1.27");
}


module.exports = {runDetectionServer, closeDetectionServer}