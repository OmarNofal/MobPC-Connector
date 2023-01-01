const {getDirectoryStructure} = require('../fs/directories');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const fs = require('fs');
const path = require('path');




router.get('/listDirectory', (req, res) => {

    const dir = req.body.path;

    if (!dir) {
        res.json(new ErrorResponse(10, "Missing [path] field"));
        return;
    }

    const files= getDirectoryStructure(dir);
    res.json(new SuccessResponse(files));

});


router.post('/mkdirs', (req, res) => {

    const body = req.body;

    const name = body.name;
    const destination = body.dest;
    
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