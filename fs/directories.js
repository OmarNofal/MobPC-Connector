const { FILE } = require('dns');
const e = require('express');
const fs = require('fs');
const path = require('path');
const {File, Directory} = require('./resource')



function getDirectoryStructure(dir) {
    const files = fs.readdirSync(dir);


    var result = [];
    for (var i = 0; i < files.length; i++) {

        const p = path.join(dir, files[i]);

        const stats = fs.lstatSync(p);
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


module.exports = { getDirectoryStructure }