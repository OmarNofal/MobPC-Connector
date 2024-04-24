import { app } from 'electron';
import { getUUID as getUID, setNewUUID, setUUIDPath, isUUIDSet } from './identification/appindentification';
//import { isLoggedIn as isLIn, isPasswordSet, changePassword as cP, setPasswordDir, logInAndGetAccessToken as logInAndGetToken} from './auth/auth';
import path from 'path';


export function init() {
    
    let appDir;
    try {
        appDir = app.getPath('userData');
    } catch (error ) {
        console.error(error);
        appDir = app.getPath('exe');
    }

    setUUIDPath(path.join(appDir, "id"));
    //setPasswordDir(appDir);

    if (!isUUIDSet()) {
        console.log("UUID not set");
        setNewUUID()
    }

    //if (!isPasswordSet()) {
    //    changePassword('00000000')
    //};
}


// export function changePassword(newPassword) {
//     return cP(newPassword);
// }

// export function isLoggedIn(token) {
//     return isLIn(token);
// }

// export function logInAndGetAccessToken(password) {
//     return logInAndGetToken(password);
// }

export function getUUID() {
    return getUID();
}

export default {
    getUUID,
    init
}