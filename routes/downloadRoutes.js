const {getDirectoryStructure} = require('../fs/directories');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const path = require('path');
var fs = require('fs');
const CombinedStream = require('combined-stream');
const {parsePath} = require('../fs/operations');
const mime = require('mime');
const authMiddleware = require('./authMiddleware');
const { env } = require("process");
const jwt = require('jsonwebtoken');
require('dotenv').config();

function handleDownloadFile(path, res) {
    console.log("Somebody downloads " + path);
    res.download(path);
}



function handleDownloadFolder(src, res) {

    const responseHeader = {
        numberOfFiles: 0,
        totalSize: 0,
        files: []
    };

    const responseStream = new CombinedStream();
    
    const dirQueue = [src];
    while (dirQueue.length > 0) {
        const currentDirectory = dirQueue.shift();

        const contents = fs.readdirSync(currentDirectory, {withFileTypes: true});

        for (const file of contents) {

            const filePath = path.join(currentDirectory, file.name)

            if (file.isDirectory()) dirQueue.push(filePath);
            else if (file.isFile()) {
                fs.createReadStream(filePath);
                
                const fileStats = fs.statSync(filePath);

                responseHeader.numberOfFiles++;
                responseHeader.totalSize += fileStats.size;
                responseHeader.files.push( { 
                    name: file.name,
                    size: fileStats.size,
                    path: path.relative(src, currentDirectory),
                    mimeType: mime.getType(file.name) 
                })

                responseStream.append(fs.createReadStream(filePath));
            }
        }
    }
    console.log(responseHeader);
    res.setHeader('Content-Type', "folder");

    const headerSizeBuffer = Buffer.allocUnsafe(8);
    headerSizeBuffer.writeBigInt64BE(BigInt(JSON.stringify(responseHeader).length), 0);
    res.write(headerSizeBuffer);
    res.write(JSON.stringify(responseHeader));
    
    responseStream.pipe(res);
}


router.get('/downloadFiles', authMiddleware, (req, res) => {

    const body = req.body;

    var src = body.src ?? req.query.src;
    src = parsePath(src);

    if (!src) {
        return res.json(new ErrorResponse(109, "Missing [src] field"))
    }
    

    if (fs.existsSync(src)) {
        const srcStats = fs.statSync(src);
        if (srcStats.isFile()) {
            return handleDownloadFile(src, res);
        } else if (srcStats.isDirectory()) {
            return handleDownloadFolder(src, res);
        }
    } else {
        res.json(new ErrorResponse(1, "This resource does not exist"))
    }

})


router.get('/getFileAccessToken', authMiddleware, (req, res) => {

    const path = req.query.src;

    if (fs.existsSync(path)) {

        const tokenPayload = {
            path: path
        }

        const token = jwt.sign(
            tokenPayload,
            env.JWT_SECRET_KEY,
            { expiresIn: '30d' }
        )
        res.json(new SuccessResponse({token: token}));
    } else {
        res.json(new ErrorResponse(1, "File does not exist"));
    }
    

});

router.get('/getFileExternal', (req, res) => {

    const token = req.query.token;
    const userPath = req.query.path;

    if (!userPath || !token) {
        return res.json(new ErrorResponse(2, "Missing token or file path"));
    }

    let payload
    try {
        payload = jwt.verify(token, env.JWT_SECRET_KEY)
    } catch (e) {
        return res.sendStatus(401);
    }

    // token ok
    const tokenPath = payload.path
    if (path.resolve(tokenPath) == path.resolve(userPath)) {
        handleDownloadFile(tokenPath, res);
    } else {
        res.status(401);
        return res.json(new ErrorResponse(3, "Path does not match the token"))
    }

})


module.exports = router;