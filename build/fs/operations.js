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
Object.defineProperty(exports, "__esModule", { value: true });
const fs = require('fs');
const path = require('path');
const { ResourceAlreadyExists } = require('./exceptions');
const copy = require('cpy');
const { DaemonicProgress } = require('fsprogress');
const trash = require('trash');
const os = require('os');
function parsePath(src) {
    if (typeof src == 'string')
        src = src.replace('~', os.homedir());
    return src;
}
// check to see if any file or dir exists
function checkIfAnyExists(src) {
    if (src instanceof String) {
        if (fs.existsSync(src))
            return true;
    }
    else if (src instanceof Array) {
        for (const path of src) {
            if (fs.existsSync(path))
                return true;
        }
    }
    return false;
}
function copyResources(src, dest, onProgress, onFinish, onError, overwrite = false) {
    if (overwrite) {
        var pathsToCheck;
        if (typeof src == 'string' || src instanceof String) {
            pathsToCheck = path.join(dest, path.parse(src).base);
            console.log(pathsToCheck);
        }
        else {
            pathsToCheck = src.map((value, index, _) => {
                return path.join(dest, path.parse(value).base);
            });
        }
        const anyExists = checkIfAnyExists(pathsToCheck);
        if (anyExists)
            throw new ResourceAlreadyExists();
    }
    const callbackOptions = {
        chunkSize: 8192,
        maxConcurrentOperation: 0
    };
    src = path.normalize(src);
    dest = path.normalize(dest);
    console.log(src);
    console.log(dest);
    new DaemonicProgress(src, dest, callbackOptions)
        .on('progress', onProgress)
        .on('done', onFinish)
        .on('error', onError)
        .start();
}
function moveResources(src, dest, onProgress, onFinish, onError, overwrite = false) {
    if (!overwrite) {
        var pathsToCheck;
        if (typeof src == 'string' || src instanceof String) {
            pathsToCheck = path.join(dest, path.parse(src).base);
        }
        else {
            pathsToCheck = src.map((value, index, _) => {
                return path.join(dest, path.parse(value).base);
            });
        }
        const anyExists = checkIfAnyExists(pathsToCheck);
        if (anyExists)
            throw new ResourceAlreadyExists();
    }
    const callbackOptions = {
        chunkSize: 8192,
        maxConcurrentOperation: 0,
        mode: 'move'
    };
    new DaemonicProgress(src, dest, callbackOptions)
        .on('progress', onProgress)
        .on('done', onFinish)
        .on('error', onError)
        .start();
}
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
                    fs.rmSync(path, { force: true, recursive: true });
                }
                else {
                    yield trash(path);
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
function renameResource(src, newName, overwrite = false) {
    const srcPath = path.parse(src);
    const srcDir = srcPath.dir;
    const destPath = path.join(srcDir, newName);
    if (!overwrite && fs.existsSync(destPath)) {
        throw new ResourceAlreadyExists();
    }
    fs.renameSync(src, destPath);
}
module.exports = { copyResources, renameResource, moveResources, deleteResources, parsePath };
