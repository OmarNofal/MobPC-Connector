"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getDirectoryStructure = void 0;
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
const resource_1 = require("./resource");
function getDirectoryStructure(dir) {
    const files = fs_1.default.readdirSync(dir);
    var result = [];
    for (var i = 0; i < files.length; i++) {
        const p = path_1.default.join(dir, files[i]);
        var stats;
        try {
            // Usually some resources will be busy or deny permission, hence the try catch
            stats = fs_1.default.lstatSync(p);
        }
        catch (err) {
            continue;
        }
        const isDir = stats.isDirectory();
        var resource;
        if (isDir) {
            const dirFiles = fs_1.default.readdirSync(p);
            resource = new resource_1.Directory(files[i], stats.size, stats.ctimeMs, stats.mtimeMs, dirFiles);
            delete resource.content;
            delete resource.size;
        }
        else {
            resource = new resource_1.File(files[i], stats.size, stats.ctimeMs, stats.mtimeMs);
        }
        result.push(resource);
    }
    return result;
}
exports.getDirectoryStructure = getDirectoryStructure;
