const {getDirectoryStructure} = require('../fs/directories');
const { ResourceAlreadyExists } = require('../fs/exceptions');
const { copyResources, renameResource, moveResources, deleteResources, parsePath } = require('../fs/operations');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const path = require('path');
const getDrives = require('../fs/drives');
const authMiddleware = require('./authMiddleware');



router.post('/copyResources', authMiddleware, (req, res) => {


    const body = req.body

    console.log(body);

    const resourcesPath = parsePath(body.src);
    const pasteDestination = parsePath(body.dest);
    const overwrite = body.overwrite ?? false;

    if (!resourcesPath || !pasteDestination) {
        res.json(new ErrorResponse(10, "Missing [src] or [dest] resources"));
        return;
    }

    
    try {
        copyResources(
            resourcesPath,
            pasteDestination,
            (theFileData, overrallsize, theFiles) => {
                const progress = {
                    currentFile: theFileData.file,
                    totalFileBytesCopied: theFileData.totalBytesCopied,
                    totalFileSize: theFileData.totalSize,
                    totalSize: overrallsize.totalSize,
                    totalBytesCopied: overrallsize.totalBytesCopied
                }
                //res.write(JSON.stringify(progress) + "\n");
            },
            (theFileData, overrallsize, theFiles) => {
                //res.write(JSON.stringify( new SuccessResponse() ));
                //res.end();
            },
            (theFileData, overallSize, theFiles) => {
                console.log(theFileData.err);
                //res.json(new ErrorResponse(1, theFileData.err))
            },
            overwrite
        )
        res.json(new SuccessResponse());
    } catch (err) {
        console.log(err);
        if (err instanceof ResourceAlreadyExists) {
            return res.json(new ErrorResponse(10, "A resource already exists. Please enable the overwrite flag"))
        }
        else return res.json(new ErrorResponse(10, "Uncaught error: " + err));
    }
})


router.post('/moveResources', authMiddleware, (req, res) => {

    const body = req.body

    const resourcesPath = body.src;
    const pasteDestination = body.dest;
    const overwrite = body.overwrite ?? false;

    if (!resourcesPath || !pasteDestination) {
        res.json(new ErrorResponse(10, "Missing [src] or [dest] resources"));
        return;
    }

    try {
        moveResources(
            resourcesPath,
            pasteDestination,
            (theFileData, overrallsize, theFiles) => {
                const progress = {
                    currentFile: theFileData.file,
                    totalFileBytesCopied: theFileData.totalBytesCopied,
                    totalFileSize: theFileData.totalSize,
                    totalSize: overrallsize.totalSize,
                    totalBytesCopied: overrallsize.totalBytesCopied
                }
                res.write(JSON.stringify(progress) + "\n");
            },
            (theFileData, overrallsize, theFiles) => {
                res.write(JSON.stringify( new SuccessResponse() ));
                res.end();
            },
            (theFileData, overallSize, theFiles) => {
                console.log(theFileData.err);
                res.json(new ErrorResponse(1, theFileData.err))
            },
            overwrite
        )
    } catch (err) {
        if (err instanceof ResourceAlreadyExists) {
            return res.json(new ErrorResponse(10, "A resource already exists. Please enable the overwrite flag"))
        }
        else return res.json(new ErrorResponse(10, "Uncaught error: " + err));
    }
})



router.post('/deleteResources', authMiddleware,(req, res) => {

    const body = req.body;

    
    var src = body.src;
    var permanentlyDelete = ((body.permanentlyDelete ?? 0) == 1) ? true : false;


    src = parsePath(src);
    
    deleteResources(src, permanentlyDelete,
        (progress) => {
            //res.write(JSON.stringify(progress) + "\n");
        },
        () => {
            res.json(new SuccessResponse());
        }
    )

});

router.post('/renameResource', authMiddleware, (req, res) => {

    const body = req.body;

    var src = body.src;
    const newName = body.newName;
    const overwrite = body.overwrite ?? false;

    src = parsePath(src);

    console.log("Renaming " + src + " to " + newName);

    try {
        renameResource(src, newName, overwrite);
    } catch (err) {
        if (err instanceof ResourceAlreadyExists) {
            res.json(new ErrorResponse(10, "This resource already exists. Enable the [overwrite] flag to overwrite but note that the old file will be lost"));            
            return;
        } else {
            res.json(new ErrorResponse(10, "Unknown error: " + err));
            return;
        }
    }
    res.json(new SuccessResponse());
})


router.get('/drives', authMiddleware, function(req, res) {
    getDrives(
        (err => {
            res.send(new ErrorResponse(1, "Failed to retrieve drives"))
        })
        , (drives => {
        // return all drive paths
        const names = drives.flatMap((d => d.mountpoints)).map((d => d.path))
        res.send(new SuccessResponse(names));
    }));
})

module.exports = router;