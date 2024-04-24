"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs_1 = __importDefault(require("fs"));
const fsprogress_1 = require("fsprogress");
const multer_1 = __importDefault(require("multer"));
const path_1 = __importDefault(require("path"));
const operations_1 = require("../fs/operations");
const response_1 = require("../model/response");
function uploadFilesController(req, res) {
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
        while (fs_1.default.existsSync(path_1.default.join(fileDestination, fileName))) {
            fileName = path_1.default.parse(fileName).name + '(1)' + path_1.default.parse(fileName).ext;
        }
        try {
            const fileTempDirectory = path_1.default.join(file.path, '../', directory);
            fs_1.default.mkdirSync(fileTempDirectory, { recursive: true });
            fs_1.default.renameSync(currentPath, path_1.default.join(fileTempDirectory, fileName));
            fs_1.default.mkdirSync(fileDestination, { recursive: true });
            new fsprogress_1.DaemonicProgress(path_1.default.join(fileTempDirectory, fileName), fileDestination, { mode: "move" }).start();
        }
        catch (e) {
            console.log(e);
        }
    }
    res.json(new response_1.SuccessResponse());
}
function addUploadRoutes(app, uploadTempDestination, authMiddleware) {
    const upload = (0, multer_1.default)({ dest: uploadTempDestination });
    app.post('/uploadFiles', authMiddleware, upload.any(), uploadFilesController);
}
exports.default = addUploadRoutes;
