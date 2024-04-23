"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
// Import the functions you need from the SDKs you need
const app_1 = require("firebase/app");
const database_1 = require("firebase/database");
const ipService_1 = __importDefault(require("./ipService"));
const appindentification_1 = require("../identification/appindentification");
const package_json_1 = __importDefault(require("../../package.json"));
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries
// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyDKOicL06qK6j9ENuKzJ1fT8NrNn0O4w3c",
    authDomain: "pc-connector-2557a.firebaseapp.com",
    databaseURL: "https://pc-connector-2557a-default-rtdb.europe-west1.firebasedatabase.app",
    projectId: "pc-connector-2557a",
    storageBucket: "pc-connector-2557a.appspot.com",
    messagingSenderId: "518476452024",
    appId: "1:518476452024:web:49d458d5e9b80df1bf644d"
};
// Initialize Firebase
const app = (0, app_1.initializeApp)(firebaseConfig);
const db = (0, database_1.getDatabase)(app);
function firebaseRoutine() {
    (0, ipService_1.default)((ip) => {
        const uuid = (0, appindentification_1.getUUID)();
        const locationRef = (0, database_1.ref)(db, uuid);
        (0, database_1.set)(locationRef, {
            ip: ip,
            port: package_json_1.default.globalPort
        });
    }, () => {
        console.error("Failed to sync ip to firebase");
    });
}
function startFirebaseService() {
    firebaseRoutine();
    const timer = setInterval(() => {
        firebaseRoutine();
    }, 20 * 60 * 1000, true);
    return timer;
}
exports.default = startFirebaseService;
