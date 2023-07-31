const { readFile, readFileSync, writeFileSync } = require("fs");
const { randomUUID } = require("crypto");


// Contains routines that get and set the UUID for the app instance on the PC
// Each PC will be identified by a (hopefully) unique ID.
// This will allow the apps to save the connection for faster access

const UUID_FILE_PATH = './uuid'
const UUID_V4_PATTERN = "/^[0-9A-F]{8}-[0-9A-F]{4}-[4][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i"


function isUUIDSet() {
    try {
        const uuid = readFileSync(UUID_FILE_PATH, {encoding: 'utf-8'});
        return RegExp(UUID_V4_PATTERN).test(uuid);
    } catch (e) {
        return false;
    }
}

function setNewUUID() {
    const uuid = randomUUID();
    try {
        writeFileSync(UUID_FILE_PATH, uuid);
        return uuid;
    } catch (e) {
        console.log(e.code);
        throw e;
    }
}

// no checking here!
function getUUID() {
    try {
        const uuid = readFileSync(UUID_FILE_PATH, {encoding: 'utf-8'});
        return uuid;
    } catch (e) {
        throw "UUID does not exist";
    }
}


module.exports = {
    getUUID, setNewUUID, isUUIDSet
}