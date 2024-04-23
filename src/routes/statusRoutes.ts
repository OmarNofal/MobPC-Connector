
import pkg from '../../package.json';
const networkInterface = require('os').networkInterfaces;
const statusRoutes = require('express').Router();
import os from 'os';
import { getUUID } from '../identification/appindentification';

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

export default statusRoutes