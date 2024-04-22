const {  app } = require('electron');
const id = require('./identification/appindentification');
const auth = require('./auth/auth');
const path = require('path');


function init() {
    
    let appDir;
    try {
        appDir = app.getPath('userData');
    } catch (error ) {
        console.error(error);
        appDir = app.getPath('exe');
    }

    id.setUUIDPath(path.join(appDir, "id"));
    auth.setPasswordDir(appDir);

    if (!id.isUUIDSet()) {
        console.log("UUID not set");
        id.setNewUUID()
    }

    if (!auth.isPasswordSet()) {
        changePassword('00000000')
    };
}


function changePassword(newPassword) {
    return auth.changePassword(newPassword);
}

function isLoggedIn(token) {
    return auth.isLoggedIn(token);
}

function logInAndGetAccessToken(password) {
    return auth.logInAndGetAccessToken(password);
}

function getUUID() {
    return id.getUUID();
}


module.exports = {
    getUUID,
    logInAndGetAccessToken,
    isLoggedIn,
    changePassword,
    init
}