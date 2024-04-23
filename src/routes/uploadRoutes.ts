import { getDirectoryStructure } from '../fs/directories'
import { Router, Request } from 'express'
import { SuccessResponse, ErrorResponse } from '../model/response'
import path from 'path'
import multer from 'multer'
import { DaemonicProgress } from 'fsprogress'
var fs = require('fs')
import { parsePath } from '../fs/operations'
import authMiddleware from './authMiddleware'


const upload = multer({ dest: 'C:\\Users\\omarw\\OneDrive\\Documents\\Programming\\PC Connector\\Backend\\temp' });

const router = Router()



router.post('/uploadFiles', authMiddleware, upload.any(), (req, res) => {

    const body = req.body; 

    const destination = parsePath(body.dest);
    
    console.log(destination)
    console.log(req.files)

    if (!destination || req.files.length == 0) {
        res.json(new ErrorResponse(10, "Missing files or destination path"));
        return
    }

    const files = req.files as Express.Multer.File[];

    console.log(req.files.length)
    for (const file of files) {
        console.log(file);

        var directory = file.fieldname;
        const fileDestination = path.join(destination, directory);

        var fileName = file.originalname;
        const currentPath = file.path;

        while (fs.existsSync(path.join(fileDestination, fileName))) {
            fileName = path.parse(fileName).name + '(1)' + path.parse(fileName).ext;
        }

        try {
            const fileTempDirectory = path.join(file.path, '../', directory)
            fs.mkdirSync(fileTempDirectory, {recursive: true})
        
            fs.renameSync(
                currentPath,
                path.join(fileTempDirectory, fileName)
            );
        
            fs.mkdirSync(fileDestination, {recursive: true})

            new DaemonicProgress(
                path.join(fileTempDirectory, fileName), 
                fileDestination,
                {mode: "move"}
            ).start()
        } catch(e) {
            console.log(e)
        }
        
    }

    res.json(new SuccessResponse());
});



export default router