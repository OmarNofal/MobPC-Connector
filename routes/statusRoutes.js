
const package = require('../package.json');
const networkInterface = require('os').networkInterfaces;
const statusRoutes = require('express').Router();

statusRoutes.get('/status', (req, res) => {
    
    const networkInterfaces = networkInterface();
    

    res.json({
        name: "PC Connector",
        version: package.version,
        port: req.socket.localPort,
        ip: req.socket.localAddress
    });
});


module.exports = statusRoutes;