"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.isServerOpen = exports.stopServer = exports.startServer = exports.events = void 0;
const electron_1 = __importDefault(require("electron"));
const events_1 = __importDefault(require("events"));
const express_1 = __importDefault(require("express"));
const fs_1 = __importDefault(require("fs"));
const http_1 = __importDefault(require("http"));
const https_1 = __importDefault(require("https"));
const path_1 = __importDefault(require("path"));
const rxjs_1 = require("rxjs");
const auth_js_1 = __importDefault(require("./auth/auth.js"));
const detectionserver_js_1 = __importDefault(require("./detectionserver.js"));
const firebase_js_1 = __importDefault(require("./firebase/firebase.js"));
const authMiddleware_js_1 = __importDefault(require("./routes/authMiddleware.js"));
const authRoutes_js_1 = __importDefault(require("./routes/authRoutes.js"));
const browserRoutes_js_1 = __importDefault(require("./routes/browserRoutes.js"));
const clipboardRoutes_js_1 = __importDefault(require("./routes/clipboardRoutes.js"));
const directoryRoutes_js_1 = __importDefault(require("./routes/directoryRoutes.js"));
const downloadRoutes_js_1 = __importDefault(require("./routes/downloadRoutes.js"));
const fileOperationsRoutes_js_1 = require("./routes/fileOperationsRoutes.js");
const fsWatcher_js_1 = __importDefault(require("./routes/fsWatcher.js"));
const osRoutes_js_1 = __importDefault(require("./routes/osRoutes.js"));
const statusRoutes_js_1 = __importDefault(require("./routes/statusRoutes.js"));
const uploadRoutes_js_1 = __importDefault(require("./routes/uploadRoutes.js"));
var privateKey = fs_1.default.readFileSync('src/cert/server.key', 'utf8');
var certificate = fs_1.default.readFileSync('src/cert/server.crt', 'utf8');
const credentials = { key: privateKey, cert: certificate };
const PORT = 6543;
const app = (0, express_1.default)();
const appDirectory = electron_1.default.app.getPath('userData');
const authManager = new auth_js_1.default(appDirectory);
const authMiddleware = (0, authMiddleware_js_1.default)(authManager.isValidToken);
app.use(express_1.default.json());
app.use(express_1.default.urlencoded({ extended: true }));
(0, downloadRoutes_js_1.default)(app, authMiddleware);
(0, directoryRoutes_js_1.default)(app, authMiddleware);
(0, clipboardRoutes_js_1.default)(app, authMiddleware);
(0, browserRoutes_js_1.default)(app, authMiddleware);
(0, statusRoutes_js_1.default)(app);
(0, osRoutes_js_1.default)(app, authMiddleware);
(0, fileOperationsRoutes_js_1.addFileOperationsRoutes)(app, authMiddleware);
(0, uploadRoutes_js_1.default)(app, path_1.default.join(appDirectory, 'uploadTemp'), authMiddleware);
(0, authRoutes_js_1.default)(app, authManager);
let detectionServerConfiguration = new rxjs_1.BehaviorSubject({ portNumber: 4285 });
let detectionServer = new detectionserver_js_1.default(detectionServerConfiguration);
let firebaseServerConfig = new rxjs_1.BehaviorSubject({ globalPort: 1919 });
let firebaseService = new firebase_js_1.default(firebaseServerConfig);
exports.events = new events_1.default();
let https_server = null;
let http_server = null;
function startServer() {
    detectionServer.run();
    firebaseService.startService();
    http_server = http_1.default.createServer(app).listen(6544);
    https_server = httpsServer.listen(PORT);
    exports.events.emit('server-started');
}
exports.startServer = startServer;
function stopServer() {
    if (https_server != null) {
        detectionServer.close();
        firebaseService.stopService();
        http_server.close();
        http_server = null;
        https_server.close();
        https_server = null;
        exports.events.emit('server-closed');
    }
}
exports.stopServer = stopServer;
function isServerOpen() {
    return https_server != null;
}
exports.isServerOpen = isServerOpen;
const fileSystemWatcherService = new fsWatcher_js_1.default();
const httpsServer = https_1.default.createServer(credentials, app);
httpsServer.on('upgrade', (request, socket, head) => {
    fileSystemWatcherService.handleIncomingConnection(request, socket, head);
});
startServer();
