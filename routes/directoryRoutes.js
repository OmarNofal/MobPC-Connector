const {getDirectoryStructure} = require('../fs/directories');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');





router.get('/listDirectory', (req, res) => {

    const dir = req.body.path;

    if (!dir) {
        res.json(new ErrorResponse(10, "Missing [path] field"));
        return;
    }

    const files= getDirectoryStructure(dir);
    res.json(new SuccessResponse(files));

});



module.exports = router;