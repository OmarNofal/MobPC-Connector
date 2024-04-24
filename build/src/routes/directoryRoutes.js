"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
const directories_1 = require("../fs/directories");
const operations_1 = require("../fs/operations");
const response_1 = require("../model/response");
function listDirectoryController(req, res) {
    let dir = req.query.path;
    if (!dir) {
        res.json(new response_1.ErrorResponse(10, "Missing [path] field"));
        return;
    }
    dir = (0, operations_1.parsePath)(dir) + '/.';
    const files = (0, directories_1.getDirectoryStructure)(path_1.default.parse(dir).dir);
    res.json(new response_1.SuccessResponse(files));
}
function mkdirsController(req, res) {
    const body = req.body;
    const name = body.name;
    const destination = (0, operations_1.parsePath)(body.dest);
    if (!name || !destination) {
        return res.json(new response_1.ErrorResponse(10, "Missing [name] or [destination]"));
    }
    const dirPath = path_1.default.join(destination, name);
    try {
        fs_1.default.mkdirSync(dirPath, { recursive: true });
        return res.json(new response_1.SuccessResponse());
    }
    catch (err) {
        return res.json(new response_1.ErrorResponse(4, "Failed to create directories: " + err));
    }
}
function addDirectoryRoutes(app, authMiddleware) {
    app.get('/listDirectory', authMiddleware, listDirectoryController);
    app.post('/mkdirs', authMiddleware, mkdirsController);
}
exports.default = addDirectoryRoutes;
