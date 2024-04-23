import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import path from 'path';
import fs from 'fs';
import { InvalidPasswordException, PasswordNotSetException } from "./exceptions";
import { env } from "process";
import { randomUUID } from "crypto";
require('dotenv').config();

let passwordPath = "pwd"
let passwordVersion = "pwd_version"

export function changePassword(newPassword) {

    if (newPassword.length < 8)
        throw new InvalidPasswordException("Password must be atleast 8 charchters long")

    const salt = bcrypt.genSaltSync();
    const encryptedPassword = bcrypt.hashSync(newPassword, salt);

    fs.writeFileSync(passwordPath, encryptedPassword);
    fs.writeFileSync(passwordVersion, randomUUID());
}

export function isPasswordSet() {
    return fs.existsSync(passwordPath) && fs.existsSync(passwordVersion);
}


export function logInAndGetAccessToken(password) {
    let encryptedPassword = "";

    try {
        encryptedPassword = fs.readFileSync(passwordPath, { encoding: 'utf-8' });
    } catch (e) {
        if (e.code == "ENOENT")
            throw new PasswordNotSetException("Password is not set");
        else throw e
    }
    console.log(encryptedPassword);
    const result = bcrypt.compareSync(password, encryptedPassword);
    if (result) {
        const version = fs.readFileSync(passwordVersion, { encoding: "utf-8" });
        return jwt.sign({ passwordVersion: version }, env.JWT_SECRET_KEY, { expiresIn: "30d" });
    } else {
        throw new InvalidPasswordException("Passwords don't match");
    }
}


export function isLoggedIn(token) {
    try {
        jwt.verify(token, env.JWT_SECRET_KEY);
    } catch (e) {
        return false;
    }
    const version = (jwt.decode(token) as {'passwordVersion': string}).passwordVersion;
    const currentPasswordVersion = fs.readFileSync(passwordVersion, { encoding: "utf-8" });
    return (version == currentPasswordVersion)
}


export function setPasswordDir(dir) {
    passwordPath = path.join(dir, 'pwd');
    passwordVersion = path.join(dir, 'pwd_version');
}
