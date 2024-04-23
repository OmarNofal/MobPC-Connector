"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const response_1 = require("../model/response");
const path_1 = __importDefault(require("path"));
const fs_1 = __importDefault(require("fs"));
const combined_stream_1 = __importDefault(require("combined-stream"));
const operations_1 = require("../fs/operations");
const mime_1 = __importDefault(require("mime"));
const authMiddleware_1 = __importDefault(require("./authMiddleware"));
const process_1 = require("process");
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
require('dotenv').config();
function handleDownloadFile(path, res) {
    console.log("Somebody downloads " + path);
    res.download(path);
}
const router = (0, express_1.Router)();
function handleDownloadFolder(src, res) {
    const responseHeader = {
        numberOfFiles: 0,
        totalSize: 0,
        files: []
    };
    const responseStream = new combined_stream_1.default();
    const dirQueue = [src];
    while (dirQueue.length > 0) {
        const currentDirectory = dirQueue.shift();
        const contents = fs_1.default.readdirSync(currentDirectory, { withFileTypes: true });
        for (const file of contents) {
            const filePath = path_1.default.join(currentDirectory, file.name);
            if (file.isDirectory())
                dirQueue.push(filePath);
            else if (file.isFile()) {
                fs_1.default.createReadStream(filePath);
                const fileStats = fs_1.default.statSync(filePath);
                responseHeader.numberOfFiles++;
                responseHeader.totalSize += fileStats.size;
                responseHeader.files.push({
                    name: file.name,
                    size: fileStats.size,
                    path: path_1.default.relative(src, currentDirectory),
                    mimeType: mime_1.default.lookup(file.name)
                });
                responseStream.append(fs_1.default.createReadStream(filePath));
            }
        }
    }
    console.log(responseHeader);
    res.setHeader('Content-Type', "folder");
    const headerSizeBuffer = Buffer.allocUnsafe(8);
    headerSizeBuffer.writeBigInt64BE(BigInt(JSON.stringify(responseHeader).length), 0);
    res.write(headerSizeBuffer);
    res.write(JSON.stringify(responseHeader));
    responseStream.pipe(res);
}
router.get('/downloadFiles', authMiddleware_1.default, (req, res) => {
    var _a;
    const body = req.body;
    var src = (_a = body.src) !== null && _a !== void 0 ? _a : req.query.src;
    src = (0, operations_1.parsePath)(src);
    if (!src) {
        return res.json(new response_1.ErrorResponse(109, "Missing [src] field"));
    }
    if (fs_1.default.existsSync(src)) {
        const srcStats = fs_1.default.statSync(src);
        if (srcStats.isFile()) {
            return handleDownloadFile(src, res);
        }
        else if (srcStats.isDirectory()) {
            return handleDownloadFolder(src, res);
        }
    }
    else {
        res.json(new response_1.ErrorResponse(1, "This resource does not exist"));
    }
});
router.get('/getFileAccessToken', authMiddleware_1.default, (req, res) => {
    const path = req.query.src;
    if (typeof path != 'string') {
        return;
    }
    if (fs_1.default.existsSync(path)) {
        const tokenPayload = {
            path: path
        };
        const token = jsonwebtoken_1.default.sign(tokenPayload, process_1.env.JWT_SECRET_KEY, { expiresIn: '30d' });
        res.json(new response_1.SuccessResponse({ token: token }));
    }
    else {
        res.json(new response_1.ErrorResponse(1, "File does not exist"));
    }
});
router.get('/getFileExternal', (req, res) => {
    const token = req.query.token;
    const userPath = req.query.path;
    if (!userPath || !token || typeof userPath != 'string' || typeof token != 'string') {
        return res.json(new response_1.ErrorResponse(2, "Missing token or file path"));
    }
    let payload;
    try {
        payload = jsonwebtoken_1.default.verify(token, process_1.env.JWT_SECRET_KEY);
    }
    catch (e) {
        return res.sendStatus(401);
    }
    // token ok
    const tokenPath = payload.path;
    if (path_1.default.resolve(tokenPath) == path_1.default.resolve(userPath)) {
        handleDownloadFile(tokenPath, res);
    }
    else {
        res.status(401);
        return res.json(new response_1.ErrorResponse(3, "Path does not match the token"));
    }
});
exports.default = router;
