import { getDirectoryStructure } from '../fs/directories'
import { Router } from 'express'
import { SuccessResponse, ErrorResponse } from '../model/response'
import fs from 'fs'
import path from 'path'
import { parsePath } from '../fs/operations'
import authMiddleware from './authMiddleware'


const router = Router()


router.get('/listDirectory', authMiddleware, (req, res) => {

    
    let dir = req.query.path;

    
    if (!dir) {
        res.json(new ErrorResponse(10, "Missing [path] field"));
        return;
    }
    
    dir = parsePath(dir) + '/.';
        

    const files= getDirectoryStructure(path.parse(dir).dir);
    res.json(new SuccessResponse(files));

});


router.post('/mkdirs', authMiddleware, (req, res) => {

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


});


export default router