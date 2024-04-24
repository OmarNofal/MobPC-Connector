import { Request, Response, Router } from 'express';
import os from 'os';
import pkg from '../../package.json';
import { getUUID } from '../identification/appindentification';


function statusController(req: Request, res: Response) {
    res.json({
        name: pkg.serverName,
        version: pkg.version,
        port: req.socket.localPort,
        ip: req.socket.localAddress,
        id: getUUID(),
        os: os.platform()
    });
}

export default function addStatusRoutes(app: Router) {
    app.get('/status', statusController)
}