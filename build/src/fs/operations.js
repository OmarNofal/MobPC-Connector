"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.renameResource = exports.deleteResources = exports.moveResources = exports.copyResources = exports.checkIfAnyExists = exports.parsePath = void 0;
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
const exceptions_1 = require("./exceptions");
const fsprogress_1 = require("fsprogress");
const trash_1 = __importDefault(require("trash"));
const os_1 = __importDefault(require("os"));
function parsePath(src) {
    if (typeof src == 'string')
        src = src.replace('~', os_1.default.homedir());
    return src;
}
exports.parsePath = parsePath;
// check to see if any file or dir exists
function checkIfAnyExists(src) {
    if (typeof src == 'string') {
        if (fs_1.default.existsSync(src))
            return true;
    }
    else if (src instanceof Array) {
        for (const path of src) {
            if (fs_1.default.existsSync(path))
                return true;
        }
    }
    return false;
}
exports.checkIfAnyExists = checkIfAnyExists;
function copyResources(src, dest, onProgress, onFinish, onError, overwrite = false) {
    if (overwrite) {
        var pathsToCheck;
        if (typeof src == 'string') {
            pathsToCheck = path_1.default.join(dest, path_1.default.parse(src).base);
            console.log(pathsToCheck);
        }
        else {
            pathsToCheck = src.map((value, index, _) => {
                return path_1.default.join(dest, path_1.default.parse(value).base);
            });
        }
        const anyExists = checkIfAnyExists(pathsToCheck);
        if (anyExists)
            throw new exceptions_1.ResourceAlreadyExists();
    }
    const callbackOptions = {
        chunkSize: 8192,
        maxConcurrentOperation: 0
    };
    src = path_1.default.normalize(src);
    dest = path_1.default.normalize(dest);
    console.log(src);
    console.log(dest);
    new fsprogress_1.DaemonicProgress(src, dest, callbackOptions)
        .on('progress', onProgress)
        .on('done', onFinish)
        .on('error', onError)
        .start();
}
exports.copyResources = copyResources;
function moveResources(src, dest, onProgress, onFinish, onError, overwrite = false) {
    if (!overwrite) {
        var pathsToCheck;
        if (typeof src == 'string') {
            pathsToCheck = path_1.default.join(dest, path_1.default.parse(src).base);
        }
        else {
            pathsToCheck = src.map((value, index, _) => {
                return path_1.default.join(dest, path_1.default.parse(value).base);
            });
        }
        const anyExists = checkIfAnyExists(pathsToCheck);
        if (anyExists)
            throw new exceptions_1.ResourceAlreadyExists();
    }
    const callbackOptions = {
        chunkSize: 8192,
        maxConcurrentOperation: 0,
        mode: 'move'
    };
    new fsprogress_1.DaemonicProgress(src, dest, callbackOptions)
        .on('progress', onProgress)
        .on('done', onFinish)
        .on('error', onError)
        .start();
}
exports.moveResources = moveResources;
function deleteResources(src, permanentlyDelete, onProgress, onFinished) {
    return __awaiter(this, void 0, void 0, function* () {
        if (typeof src == 'string' || src instanceof String) {
            src = [src];
        }
        const numberOfResources = src.length;
        var deletedFiles = 0;
        for (const path of src) {
            try {
                if (permanentlyDelete) {
                    fs_1.default.rmSync(path, { force: true, recursive: true });
                }
                else {
                    yield (0, trash_1.default)(path);
                }
                deletedFiles++;
                onProgress({ totalDeleted: deletedFiles, totalResources: numberOfResources });
            }
            catch (err) {
            }
        }
        onFinished();
    });
}
exports.deleteResources = deleteResources;
function renameResource(src, newName, overwrite = false) {
    const srcPath = path_1.default.parse(src);
    const srcDir = srcPath.dir;
    const destPath = path_1.default.join(srcDir, newName);
    if (!overwrite && fs_1.default.existsSync(destPath)) {
        throw new exceptions_1.ResourceAlreadyExists();
    }
    fs_1.default.renameSync(src, destPath);
}
exports.renameResource = renameResource;
