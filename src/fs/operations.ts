import fs from 'fs';
import path from 'path';
import { ResourceAlreadyExists } from './exceptions';
import copy from 'cpy';
import { DaemonicProgress } from 'fsprogress';
import trash from 'trash';
import os from 'os';


export function parsePath(src) {
    if (typeof src == 'string') 
        src = src.replace('~', os.homedir())
    return src
}

// check to see if any file or dir exists
export function checkIfAnyExists(src) {
    if (typeof src == 'string') {
        if (fs.existsSync(src)) return true;
    } else if (src instanceof Array) {
        for (const path of src) {
            if (fs.existsSync(path)) return true;
        }
    }
    return false;
}

export function copyResources(src, dest, onProgress, onFinish, onError ,overwrite = false) {


    if (overwrite) {
        var pathsToCheck;
        if (typeof src == 'string') {
            pathsToCheck = path.join(dest, path.parse(src).base);
            console.log(pathsToCheck)
        } else {
            pathsToCheck = src.map((value, index, _) => {
                return path.join(dest, path.parse(value).base);
            })
        }

        const anyExists = checkIfAnyExists(pathsToCheck)
        if (anyExists) throw new ResourceAlreadyExists()
    }


    const callbackOptions = {
        chunkSize: 8192,
        maxConcurrentOperation: 0
    };
      
    src = path.normalize(src)
    dest = path.normalize(dest)
    
    console.log(src);
    console.log(dest);

    new DaemonicProgress(src, dest, callbackOptions)
        .on('progress', onProgress)
        .on('done', onFinish)
        .on('error', onError)
        .start()

}

export function moveResources(src, dest, onProgress, onFinish, onError ,overwrite = false) {

    if (!overwrite) {
        var pathsToCheck;
        if (typeof src == 'string') {
            pathsToCheck = path.join(dest, path.parse(src).base);
        } else {
            pathsToCheck = src.map((value, index, _) => {
                return path.join(dest, path.parse(value).base);
            })
        }

        const anyExists = checkIfAnyExists(pathsToCheck)
        if (anyExists) throw new ResourceAlreadyExists()
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
        .start()

}


export async function deleteResources(src, permanentlyDelete, onProgress, onFinished) {

    if (typeof src == 'string' || src instanceof String) {
        src = [src];
    }

    const numberOfResources = src.length;
    var deletedFiles = 0;
    for (const path of src) {
        try {
            if (permanentlyDelete) {
                fs.rmSync(path, {force: true, recursive: true});
            } else {
                await trash(path);
            }
            deletedFiles++;
            onProgress({totalDeleted: deletedFiles, totalResources: numberOfResources})
        } catch (err) {

        }
    }

    onFinished();
}


export function renameResource(
    src, 
    newName, 
    overwrite = false
    ) {

    const srcPath = path.parse(src);
    const srcDir = srcPath.dir;
    const destPath = path.join(srcDir, newName);

    if (!overwrite && fs.existsSync(destPath)) {
        throw new ResourceAlreadyExists();
    }

    fs.renameSync(src, destPath);
}
