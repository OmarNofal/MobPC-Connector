const {getDirectoryStructure} = require('../fs/directories');
const { ResourceAlreadyExists } = require('../fs/exceptions');
const { copyResources, renameResource, moveResources, deleteResources } = require('../fs/operations');
const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const path = require('path');



router.post('/copyResources', (req, res) => {

    const body = req.body

    const resourcesPath = body.src;
    const pasteDestination = body.dest;
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


router.post('/moveResources', (req, res) => {

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



router.post('/deleteResources', (req, res) => {
    const body = req.body;

    const src = body.src;
    const permanentlyDelete = body.permanentlyDelete ?? false;
    
    deleteResources(src, permanentlyDelete,
        (progress) => {
            res.write(JSON.stringify(progress) + "\n");
        },
        () => {
            res.write(new SuccessResponse());
            res.end();
        }
    )

});

router.post('/renameResource', (req, res) => {

    const body = req.body;

    const src = body.src;
    const newName = body.newName;
    const overwrite = body.overwrite ?? false;

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


module.exports = router;