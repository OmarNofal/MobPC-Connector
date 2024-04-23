"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.setUUIDPath = exports.getUUID = exports.setNewUUID = exports.isUUIDSet = void 0;
const fs_1 = require("fs");
const crypto_1 = require("crypto");
// Contains routines that get and set the UUID for the app instance on the PC
// Each PC will be identified by a (hopefully) unique ID.
// This will allow the apps to save the connection for faster access
let uuidFilePath = './uuid';
const UUID_V4_PATTERN = "/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i";
function isUUIDSet() {
    try {
        return (0, fs_1.existsSync)(uuidFilePath);
    }
    catch (e) {
        console.log(e);
        return false;
    }
}
exports.isUUIDSet = isUUIDSet;
function setNewUUID() {
    const uuid = (0, crypto_1.randomUUID)();
    try {
        (0, fs_1.writeFileSync)(uuidFilePath, uuid);
        return uuid;
    }
    catch (e) {
        console.log(e.code);
        throw e;
    }
}
exports.setNewUUID = setNewUUID;
// no checking here!
function getUUID() {
    try {
        const uuid = (0, fs_1.readFileSync)(uuidFilePath, { encoding: 'utf-8' });
        return uuid;
    }
    catch (e) {
        throw "UUID does not exist";
    }
}
exports.getUUID = getUUID;
function setUUIDPath(path) {
    uuidFilePath = path;
}
exports.setUUIDPath = setUUIDPath;
