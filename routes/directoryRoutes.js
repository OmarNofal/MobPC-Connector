const {getDirectoryStructure} = require('../fs/directories');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const fs = require('fs');
const path = require('path');
const os = require('os');
const { parsePath } = require('../fs/operations');
const authMiddleware = require('./authMiddleware');



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


module.exports = router;