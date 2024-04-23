"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const directories_1 = require("../fs/directories");
const express_1 = require("express");
const response_1 = require("../model/response");
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
const operations_1 = require("../fs/operations");
const authMiddleware_1 = __importDefault(require("./authMiddleware"));
const router = (0, express_1.Router)();
router.get('/listDirectory', authMiddleware_1.default, (req, res) => {
    let dir = req.query.path;
    if (!dir) {
        res.json(new response_1.ErrorResponse(10, "Missing [path] field"));
        return;
    }
    dir = (0, operations_1.parsePath)(dir) + '/.';
    const files = (0, directories_1.getDirectoryStructure)(path_1.default.parse(dir).dir);
    res.json(new response_1.SuccessResponse(files));
});
router.post('/mkdirs', authMiddleware_1.default, (req, res) => {
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
});
exports.default = router;
