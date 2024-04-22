const { readFile, readFileSync, writeFileSync, existsSync } = require("fs");
const { randomUUID } = require("crypto");


// Contains routines that get and set the UUID for the app instance on the PC
// Each PC will be identified by a (hopefully) unique ID.
// This will allow the apps to save the connection for faster access

let uuidFilePath = './uuid'
const UUID_V4_PATTERN = "/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i"


function isUUIDSet() {
    try {
        return existsSync(uuidFilePath);
    } catch (e) {
        console.log(e);
        return false;
    }
}

function setNewUUID() {
    const uuid = randomUUID();
    try {
        writeFileSync(uuidFilePath, uuid);
        return uuid;
    } catch (e) {
        console.log(e.code);
        throw e;
    }
}

// no checking here!
function getUUID() {
    try {
        const uuid = readFileSync(uuidFilePath, {encoding: 'utf-8'});
        return uuid;
    } catch (e) {
        throw "UUID does not exist";
    }
}


function setUUIDPath(path) {
    uuidFilePath = path;
}

module.exports = {
    getUUID, setNewUUID, isUUIDSet,
    setUUIDPath
}