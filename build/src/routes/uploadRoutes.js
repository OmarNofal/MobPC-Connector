"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const response_1 = require("../model/response");
const path_1 = __importDefault(require("path"));
const multer_1 = __importDefault(require("multer"));
const fsprogress_1 = require("fsprogress");
var fs = require('fs');
const operations_1 = require("../fs/operations");
const authMiddleware_1 = __importDefault(require("./authMiddleware"));
const upload = (0, multer_1.default)({ dest: 'C:\\Users\\omarw\\OneDrive\\Documents\\Programming\\PC Connector\\Backend\\temp' });
const router = (0, express_1.Router)();
router.post('/uploadFiles', authMiddleware_1.default, upload.any(), (req, res) => {
    const body = req.body;
    const destination = (0, operations_1.parsePath)(body.dest);
    console.log(destination);
    console.log(req.files);
    if (!destination || req.files.length == 0) {
        res.json(new response_1.ErrorResponse(10, "Missing files or destination path"));
        return;
    }
    const files = req.files;
    console.log(req.files.length);
    for (const file of files) {
        console.log(file);
        var directory = file.fieldname;
        const fileDestination = path_1.default.join(destination, directory);
        var fileName = file.originalname;
        const currentPath = file.path;
        while (fs.existsSync(path_1.default.join(fileDestination, fileName))) {
            fileName = path_1.default.parse(fileName).name + '(1)' + path_1.default.parse(fileName).ext;
        }
        try {
            const fileTempDirectory = path_1.default.join(file.path, '../', directory);
            fs.mkdirSync(fileTempDirectory, { recursive: true });
            fs.renameSync(currentPath, path_1.default.join(fileTempDirectory, fileName));
            fs.mkdirSync(fileDestination, { recursive: true });
            new fsprogress_1.DaemonicProgress(path_1.default.join(fileTempDirectory, fileName), fileDestination, { mode: "move" }).start();
        }
        catch (e) {
            console.log(e);
        }
    }
    res.json(new response_1.SuccessResponse());
});
exports.default = router;
