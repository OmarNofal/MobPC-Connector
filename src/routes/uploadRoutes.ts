const {getDirectoryStructure} = require('../fs/directories');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const path = require('path');
const multer = require('multer');
const { DaemonicProgress } = require('fsprogress')
var fs = require('fs')
const { parsePath } = require('../fs/operations');
const authMiddleware = require('./authMiddleware');

const upload = multer({ dest: 'C:\\Users\\omarw\\OneDrive\\Documents\\Programming\\PC Connector\\Backend\\temp' });

router.post('/uploadFiles', authMiddleware, upload.any(), (req, res) => {

    const body = req.body; 

    const destination = parsePath(body.dest);
    
    console.log(destination)
    console.log(req.files)

    if (!destination || req.files.length == 0) {
        res.json(new ErrorResponse(10, "Missing files or destination path"));
        return
    }

    console.log(req.files.length)
    for (const file of req.files) {
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



module.exports = router;