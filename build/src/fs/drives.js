"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const drivelist_1 = __importDefault(require("drivelist"));
function getDrives(onError, onSuccess) {
    drivelist_1.default.list((err, devices) => {
        if (err)
            onError(err);
        else
            onSuccess(devices);
    });
}
exports.default = getDrives;
