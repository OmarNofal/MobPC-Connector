"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getUUID = exports.init = void 0;
const electron_1 = require("electron");
const appindentification_1 = require("./identification/appindentification");
//import { isLoggedIn as isLIn, isPasswordSet, changePassword as cP, setPasswordDir, logInAndGetAccessToken as logInAndGetToken} from './auth/auth';
const path_1 = __importDefault(require("path"));
function init() {
    let appDir;
    try {
        appDir = electron_1.app.getPath('userData');
    }
    catch (error) {
        console.error(error);
        appDir = electron_1.app.getPath('exe');
    }
    (0, appindentification_1.setUUIDPath)(path_1.default.join(appDir, "id"));
    //setPasswordDir(appDir);
    if (!(0, appindentification_1.isUUIDSet)()) {
        console.log("UUID not set");
        (0, appindentification_1.setNewUUID)();
    }
    //if (!isPasswordSet()) {
    //    changePassword('00000000')
    //};
}
exports.init = init;
// export function changePassword(newPassword) {
//     return cP(newPassword);
// }
// export function isLoggedIn(token) {
//     return isLIn(token);
// }
// export function logInAndGetAccessToken(password) {
//     return logInAndGetToken(password);
// }
function getUUID() {
    return (0, appindentification_1.getUUID)();
}
exports.getUUID = getUUID;
exports.default = {
    getUUID,
    init
};
