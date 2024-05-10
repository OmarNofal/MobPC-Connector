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

    if (!isUUIDSet()) {
        console.log("UUID not set");
        setNewUUID()
    }
}

export function getUUID() {
    return getUID();
}

export default {
    getUUID,
    init
}