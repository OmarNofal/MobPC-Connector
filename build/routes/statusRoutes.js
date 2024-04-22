"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const pkg = require('../../package.json');
const networkInterface = require('os').networkInterfaces;
const statusRoutes = require('express').Router();
const os = require('os');
const { getUUID } = require('../identification/appindentification');
statusRoutes.get('/status', (req, res) => {
    const networkInterfaces = networkInterface();
    res.json({
        name: pkg.serverName,
        version: pkg.version,
        port: req.socket.localPort,
        ip: req.socket.localAddress,
        id: getUUID(),
        os: os.platform()
    });
});
module.exports = statusRoutes;
