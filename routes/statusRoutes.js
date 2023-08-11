
const package = require('../package.json');
const networkInterface = require('os').networkInterfaces;
const statusRoutes = require('express').Router();
const os = require('os');
const {getUUID} = require('../identification/appindentification');

statusRoutes.get('/status', (req, res) => {
    
    const networkInterfaces = networkInterface();
    

    res.json({
        name: package.serverName,
        version: package.version,
        port: req.socket.localPort,
        ip: req.socket.localAddress,
        id: getUUID(),
        os: os.platform()
    });
});


module.exports = statusRoutes;