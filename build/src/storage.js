"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getUUID = exports.logInAndGetAccessToken = exports.isLoggedIn = exports.changePassword = exports.init = void 0;
const electron_1 = require("electron");
const appindentification_1 = require("./identification/appindentification");
const auth_1 = require("./auth/auth");
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
    (0, auth_1.setPasswordDir)(appDir);
    if (!(0, appindentification_1.isUUIDSet)()) {
        console.log("UUID not set");
        (0, appindentification_1.setNewUUID)();
    }
    if (!(0, auth_1.isPasswordSet)()) {
        changePassword('00000000');
    }
    ;
}
exports.init = init;
function changePassword(newPassword) {
    return (0, auth_1.changePassword)(newPassword);
}
exports.changePassword = changePassword;
function isLoggedIn(token) {
    return (0, auth_1.isLoggedIn)(token);
}
exports.isLoggedIn = isLoggedIn;
function logInAndGetAccessToken(password) {
    return (0, auth_1.logInAndGetAccessToken)(password);
}
exports.logInAndGetAccessToken = logInAndGetAccessToken;
function getUUID() {
    return (0, appindentification_1.getUUID)();
}
exports.getUUID = getUUID;
exports.default = {
    init, changePassword, isLoggedIn, logInAndGetAccessToken, getUUID
};
