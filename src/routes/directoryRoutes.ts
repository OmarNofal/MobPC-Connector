import { Request, Response, Router } from 'express'
import fs from 'fs'
import path from 'path'
import { getDirectoryStructure } from '../fs/directories'
import { parsePath } from '../fs/operations'
import { ErrorResponse, SuccessResponse } from '../model/response'
import { AuthMiddlewareFunction } from './authMiddleware'


function listDirectoryController(req: Request, res: Response) {
    let dir = req.query.path;

    
    if (!dir) {
        res.json(new ErrorResponse(10, "Missing [path] field"));
        return;
    }
    
    dir = parsePath(dir) + '/.';
        

    const files= getDirectoryStructure(path.parse(dir).dir);
    res.json(new SuccessResponse(files));
}

function mkdirsController(req: Request, res: Response) {
    const body = req.body;

    const name = body.name;
    const destination = parsePath(body.dest);
    
    if (!name || !destination) {
        return res.json(new ErrorResponse(10, "Missing [name] or [destination]"));
    }

    const dirPath = path.join(destination, name);
    
    try {
        fs.mkdirSync(dirPath, {recursive: true});
        return res.json(new SuccessResponse());
    }
    catch(err) {
        return res.json(new ErrorResponse(4, "Failed to create directories: " + err));
    }
}



export default function addDirectoryRoutes(
    app: Router,
    authMiddleware: AuthMiddlewareFunction
) {
    app.get('/listDirectory', authMiddleware, listDirectoryController)
    app.post('/mkdirs', authMiddleware, mkdirsController)
}