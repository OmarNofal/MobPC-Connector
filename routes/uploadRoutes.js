const {getDirectoryStructure} = require('../fs/directories');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const path = require('path');
const multer = require('multer');
const { DaemonicProgress } = require('fsprogress')
var fs = require('fs')

const upload = multer({ dest: 'C:\\Users\\omarw\\OneDrive\\Documents\\Programming\\PC Connector\\Backend\\temp' });

router.post('/uploadFiles', upload.any(), (req, res) => {

    const body = req.body;

    const destination = body.dest;
    
    if (!destination || req.files.length == 0) {
        res.json(new ErrorResponse(10, "Missing files or destination path"));
    }

    for (const file of req.files) {
        console.log(file);

        var directory = file.fieldname;
        const fileDestination = path.join(destination, directory);

        var fileName = file.originalname;
        const currentPath = file.path;

        while (fs.existsSync(path.join(fileDestination, fileName))) {
            fileName = path.parse(fileName).name + '(1)' + path.parse(fileName).ext;
        }

        fs.renameSync(
            currentPath,
            path.join(file.path, '../', fileName)
        );

        fs.mkdirSync(fileDestination, {recursive: true})

        new DaemonicProgress(
            path.join(file.path, '../', fileName), 
            fileDestination,
            {mode: "move"}
        ).start()

    }

    res.json(new SuccessResponse());
});



module.exports = router;