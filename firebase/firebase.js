// Import the functions you need from the SDKs you need
const { initializeApp, deleteApp } = require("firebase/app");
const { getDatabase, set, ref } = require('firebase/database')
const getIp = require('./ipService');
const { getUUID, setNewUUID } = require('../identification/appindentification');
const config = require('../package.json')
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
const app = initializeApp(firebaseConfig);

const db = getDatabase(app);


function firebaseRoutine() {
    getIp(
    (ip) => {
        const uuid = getUUID();
        const locationRef = ref(db, uuid);
        set(locationRef, {
            ip: ip,
            port: config.globalPort
        })
    },
    () => {
        console.error("Failed to sync ip to firebase");
    }
)
}

function startFirebaseService() {
    const timer = setInterval(
        () => {
            firebaseRoutine();
        },
        20 * 60 * 1000,
        true
    );
    return timer;
}

module.exports = startFirebaseService;