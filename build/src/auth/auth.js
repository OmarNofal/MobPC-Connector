"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.setPasswordDir = exports.isLoggedIn = exports.logInAndGetAccessToken = exports.isPasswordSet = exports.changePassword = void 0;
const bcrypt_1 = __importDefault(require("bcrypt"));
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const path_1 = __importDefault(require("path"));
const fs_1 = __importDefault(require("fs"));
const exceptions_1 = require("./exceptions");
const process_1 = require("process");
const crypto_1 = require("crypto");
require('dotenv').config();
let passwordPath = "pwd";
let passwordVersion = "pwd_version";
function changePassword(newPassword) {
    if (newPassword.length < 8)
        throw new exceptions_1.InvalidPasswordException("Password must be atleast 8 charchters long");
    const salt = bcrypt_1.default.genSaltSync();
    const encryptedPassword = bcrypt_1.default.hashSync(newPassword, salt);
    fs_1.default.writeFileSync(passwordPath, encryptedPassword);
    fs_1.default.writeFileSync(passwordVersion, (0, crypto_1.randomUUID)());
}
exports.changePassword = changePassword;
function isPasswordSet() {
    return fs_1.default.existsSync(passwordPath) && fs_1.default.existsSync(passwordVersion);
}
exports.isPasswordSet = isPasswordSet;
function logInAndGetAccessToken(password) {
    let encryptedPassword = "";
    try {
        encryptedPassword = fs_1.default.readFileSync(passwordPath, { encoding: 'utf-8' });
    }
    catch (e) {
        if (e.code == "ENOENT")
            throw new exceptions_1.PasswordNotSetException("Password is not set");
        else
            throw e;
    }
    console.log(encryptedPassword);
    const result = bcrypt_1.default.compareSync(password, encryptedPassword);
    if (result) {
        const version = fs_1.default.readFileSync(passwordVersion, { encoding: "utf-8" });
        return jsonwebtoken_1.default.sign({ passwordVersion: version }, process_1.env.JWT_SECRET_KEY, { expiresIn: "30d" });
    }
    else {
        throw new exceptions_1.InvalidPasswordException("Passwords don't match");
    }
}
exports.logInAndGetAccessToken = logInAndGetAccessToken;
function isLoggedIn(token) {
    try {
        jsonwebtoken_1.default.verify(token, process_1.env.JWT_SECRET_KEY);
    }
    catch (e) {
        return false;
    }
    const version = jsonwebtoken_1.default.decode(token).passwordVersion;
    const currentPasswordVersion = fs_1.default.readFileSync(passwordVersion, { encoding: "utf-8" });
    return (version == currentPasswordVersion);
}
exports.isLoggedIn = isLoggedIn;
function setPasswordDir(dir) {
    passwordPath = path_1.default.join(dir, 'pwd');
    passwordVersion = path_1.default.join(dir, 'pwd_version');
}
exports.setPasswordDir = setPasswordDir;
