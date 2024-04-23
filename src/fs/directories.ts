const { FILE } = require('dns');
const e = require('express');
const fs = require('fs');
const path = require('path');
const {File, Directory} = require('./resource')



export function getDirectoryStructure(dir) {
    const files = fs.readdirSync(dir);


    var result = [];
    for (var i = 0; i < files.length; i++) {

        const p = path.join(dir, files[i]);

        var stats;
        try {
            // Usually some resources will be busy or deny permission, hence the try catch
            stats = fs.lstatSync(p);
        } catch(err) {
            continue
        }
        const isDir = stats.isDirectory();

        var resource;
        if (isDir) {
            const dirFiles = fs.readdirSync(p);
            resource = new Directory(
                files[i],
                stats.size,
                stats.ctimeMs,
                stats.mtimeMs,
                dirFiles
            )
            delete resource.content;
            delete resource.size;
        } else {
            resource = new File(
                files[i],
                stats.size,
                stats.ctimeMs,
                stats.mtimeMs
            );
        }

        result.push(resource);
    }

    return result;
}