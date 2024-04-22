"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const drivelist = require('drivelist');
//const driveInfo = require('diskusage');
function getDrives(onError, onSuccess) {
    drivelist.list((err, devices) => {
        if (err)
            onError(err);
        else
            onSuccess(devices);
    });
}
module.exports = getDrives;
