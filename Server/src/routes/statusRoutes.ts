import { Request, Response, Router } from 'express';
import os from 'os';
import pkg from '../../package.json';



function statusControllerProvider(uuidProvider: () => string) {
    return (req: Request, res: Response) => {
        res.json({
            name: pkg.serverName,
            version: pkg.version,
            port: req.socket.localPort,
            ip: req.socket.localAddress,
            id: uuidProvider(),
            os: os.platform()
        });
    }
}

export default function addStatusRoutes(app: Router, uuidProvider: () => string) {
    app.get('/status', statusControllerProvider(uuidProvider))
}