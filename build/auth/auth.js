"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const bcrypt_1 = __importDefault(require("bcrypt"));
const jwt = require("jsonwebtoken");
const path = require('path');
const fs = require('fs');
const { assert } = require("console");
const { InvalidPasswordException, PasswordNotSetException } = require("./exceptions");
const { env } = require("process");
const { randomUUID } = require("crypto");
require('dotenv').config();
let passwordPath = "pwd";
let passwordVersion = "pwd_version";
function changePassword(newPassword) {
    if (newPassword.length < 8)
        throw new InvalidPasswordException("Password must be atleast 8 charchters long");
    const salt = bcrypt_1.default.genSaltSync();
    const encryptedPassword = bcrypt_1.default.hashSync(newPassword, salt);
    fs.writeFileSync(passwordPath, encryptedPassword);
    fs.writeFileSync(passwordVersion, randomUUID());
}
function isPasswordSet() {
    return fs.existsSync(passwordPath) && fs.existsSync(passwordVersion);
}
function logInAndGetAccessToken(password) {
    let encryptedPassword = "";
    try {
        encryptedPassword = fs.readFileSync(passwordPath, { encoding: 'utf-8' });
    }
    catch (e) {
        if (e.code == "ENOENT")
            throw new PasswordNotSetException("Password is not set");
        else
            throw e;
    }
    console.log(encryptedPassword);
    const result = bcrypt_1.default.compareSync(password, encryptedPassword);
    if (result) {
        const version = fs.readFileSync(passwordVersion, { encoding: "utf-8" });
        return jwt.sign({ passwordVersion: version }, env.JWT_SECRET_KEY, { expiresIn: "30d" });
    }
    else {
        throw new InvalidPasswordException("Passwords don't match");
    }
}
function isLoggedIn(token) {
    try {
        jwt.verify(token, env.JWT_SECRET_KEY);
    }
    catch (e) {
        return false;
    }
    const version = jwt.decode(token).passwordVersion;
    const currentPasswordVersion = fs.readFileSync(passwordVersion, { encoding: "utf-8" });
    return (version == currentPasswordVersion);
}
function setPasswordDir(dir) {
    passwordPath = path.join(dir, 'pwd');
    passwordVersion = path.join(dir, 'pwd_version');
}
module.exports = {
    isLoggedIn,
    logInAndGetAccessToken,
    changePassword,
    setPasswordDir,
    isPasswordSet
};
