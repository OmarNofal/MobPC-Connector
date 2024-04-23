"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const exceptions_1 = require("../fs/exceptions");
const operations_1 = require("../fs/operations");
const express_1 = require("express");
const response_1 = require("../model/response");
const drives_1 = __importDefault(require("../fs/drives"));
const authMiddleware_1 = __importDefault(require("./authMiddleware"));
const router = (0, express_1.Router)();
router.post('/copyResources', authMiddleware_1.default, (req, res) => {
    var _a;
    const body = req.body;
    console.log(body);
    const resourcesPath = (0, operations_1.parsePath)(body.src);
    const pasteDestination = (0, operations_1.parsePath)(body.dest);
    const overwrite = (_a = body.overwrite) !== null && _a !== void 0 ? _a : false;
    if (!resourcesPath || !pasteDestination) {
        res.json(new response_1.ErrorResponse(10, "Missing [src] or [dest] resources"));
        return;
    }
    try {
        (0, operations_1.copyResources)(resourcesPath, pasteDestination, (theFileData, overrallsize, theFiles) => {
            const progress = {
                currentFile: theFileData.file,
                totalFileBytesCopied: theFileData.totalBytesCopied,
                totalFileSize: theFileData.totalSize,
                totalSize: overrallsize.totalSize,
                totalBytesCopied: overrallsize.totalBytesCopied
            };
            //res.write(JSON.stringify(progress) + "\n");
        }, (theFileData, overrallsize, theFiles) => {
            //res.write(JSON.stringify( new SuccessResponse() ));
            //res.end();
        }, (theFileData, overallSize, theFiles) => {
            console.log(theFileData.err);
            //res.json(new ErrorResponse(1, theFileData.err))
        }, overwrite);
        res.json(new response_1.SuccessResponse());
    }
    catch (err) {
        console.log(err);
        if (err instanceof exceptions_1.ResourceAlreadyExists) {
            return res.json(new response_1.ErrorResponse(10, "A resource already exists. Please enable the overwrite flag"));
        }
        else
            return res.json(new response_1.ErrorResponse(10, "Uncaught error: " + err));
    }
});
router.post('/moveResources', authMiddleware_1.default, (req, res) => {
    var _a;
    const body = req.body;
    const resourcesPath = body.src;
    const pasteDestination = body.dest;
    const overwrite = (_a = body.overwrite) !== null && _a !== void 0 ? _a : false;
    if (!resourcesPath || !pasteDestination) {
        res.json(new response_1.ErrorResponse(10, "Missing [src] or [dest] resources"));
        return;
    }
    try {
        (0, operations_1.moveResources)(resourcesPath, pasteDestination, (theFileData, overrallsize, theFiles) => {
            const progress = {
                currentFile: theFileData.file,
                totalFileBytesCopied: theFileData.totalBytesCopied,
                totalFileSize: theFileData.totalSize,
                totalSize: overrallsize.totalSize,
                totalBytesCopied: overrallsize.totalBytesCopied
            };
            res.write(JSON.stringify(progress) + "\n");
        }, (theFileData, overrallsize, theFiles) => {
            res.write(JSON.stringify(new response_1.SuccessResponse()));
            res.end();
        }, (theFileData, overallSize, theFiles) => {
            console.log(theFileData.err);
            res.json(new response_1.ErrorResponse(1, theFileData.err));
        }, overwrite);
    }
    catch (err) {
        if (err instanceof exceptions_1.ResourceAlreadyExists) {
            return res.json(new response_1.ErrorResponse(10, "A resource already exists. Please enable the overwrite flag"));
        }
        else
            return res.json(new response_1.ErrorResponse(10, "Uncaught error: " + err));
    }
});
router.post('/deleteResources', authMiddleware_1.default, (req, res) => {
    var _a;
    const body = req.body;
    var src = body.src;
    var permanentlyDelete = (((_a = body.permanentlyDelete) !== null && _a !== void 0 ? _a : 0) == 1) ? true : false;
    src = (0, operations_1.parsePath)(src);
    (0, operations_1.deleteResources)(src, permanentlyDelete, (progress) => {
        //res.write(JSON.stringify(progress) + "\n");
    }, () => {
        res.json(new response_1.SuccessResponse());
    });
});
router.post('/renameResource', authMiddleware_1.default, (req, res) => {
    var _a;
    const body = req.body;
    var src = body.src;
    const newName = body.newName;
    const overwrite = (_a = body.overwrite) !== null && _a !== void 0 ? _a : false;
    src = (0, operations_1.parsePath)(src);
    console.log("Renaming " + src + " to " + newName);
    try {
        (0, operations_1.renameResource)(src, newName, overwrite);
    }
    catch (err) {
        if (err instanceof exceptions_1.ResourceAlreadyExists) {
            res.json(new response_1.ErrorResponse(10, "This resource already exists. Enable the [overwrite] flag to overwrite but note that the old file will be lost"));
            return;
        }
        else {
            res.json(new response_1.ErrorResponse(10, "Unknown error: " + err));
            return;
        }
    }
    res.json(new response_1.SuccessResponse());
});
router.get('/drives', authMiddleware_1.default, function (req, res) {
    (0, drives_1.default)((err => {
        res.send(new response_1.ErrorResponse(1, "Failed to retrieve drives"));
    }), (drives => {
        // return all drive paths
        const names = drives.flatMap((d => d.mountpoints)).map((d => d.path));
        res.send(new response_1.SuccessResponse(names));
    }));
});
exports.default = router;
