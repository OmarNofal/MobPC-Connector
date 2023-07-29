const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const fs = require('fs');
const { assert } = require("console");
const { InvalidPasswordException, PasswordNotSetException } = require("./exceptions");
const { env } = require("process");
const { randomUUID } = require("crypto");
require('dotenv').config();

const PASSWORD_FILE_NAME = "pwd"
const PASSWORD_VERSION_FILE_NAME = "pwd_version"

function changePassword(newPassword) {

    if (newPassword.length < 8)
        throw new InvalidPasswordException("Password must be atleast 8 charchters long")

    const salt = bcrypt.genSaltSync();
    const encryptedPassword = bcrypt.hashSync(newPassword, salt);

    fs.writeFileSync(PASSWORD_FILE_NAME, encryptedPassword);
    fs.writeFileSync(PASSWORD_VERSION_FILE_NAME, randomUUID());
}


function logInAndGetAccessToken(password) {
    let encryptedPassword = "";

    try {
        encryptedPassword = fs.readFileSync(PASSWORD_FILE_NAME, { encoding: 'utf-8'});
    } catch (e) {
        if (e.code == "ENOENT")
            throw new PasswordNotSetException("Password is not set");
        else throw e
    }
    const result = bcrypt.compareSync(password, encryptedPassword);
    if (result) {
        const passwordVersion = fs.readFileSync(PASSWORD_VERSION_FILE_NAME, { encoding: "utf-8" });
        return jwt.sign({passwordVersion: passwordVersion}, env.JWT_SECRET_KEY, {expiresIn: "30d"});
    } else {
        throw new InvalidPasswordException("Passwords don't match");
    }
}


function isLoggedIn(token) {
    try {
        jwt.verify(token, env.JWT_SECRET_KEY);
    } catch (e) {
        return false;
    }
    const passwordVersion = jwt.decode(token).passwordVersion;
    const currentPasswordVersion = fs.readFileSync(PASSWORD_VERSION_FILE_NAME, {encoding: "utf-8"});
    return (passwordVersion == currentPasswordVersion)
}


module.exports = {
    isLoggedIn, 
    logInAndGetAccessToken,
    changePassword
}