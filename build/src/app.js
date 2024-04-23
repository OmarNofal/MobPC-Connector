"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.isServerOpen = exports.stopServer = exports.startServer = exports.events = void 0;
const express_1 = __importDefault(require("express"));
const https_1 = __importDefault(require("https"));
const statusRoutes_js_1 = __importDefault(require("./routes/statusRoutes.js"));
const browserRoutes_js_1 = __importDefault(require("./routes/browserRoutes.js"));
const clipboardRoutes_js_1 = __importDefault(require("./routes/clipboardRoutes.js"));
const directoryRoutes_js_1 = __importDefault(require("./routes/directoryRoutes.js"));
const osRoutes_js_1 = __importDefault(require("./routes/osRoutes.js"));
const fileOperationsRoutes_js_1 = __importDefault(require("./routes/fileOperationsRoutes.js"));
const uploadRoutes_js_1 = __importDefault(require("./routes/uploadRoutes.js"));
const downloadRoutes_js_1 = __importDefault(require("./routes/downloadRoutes.js"));
const authRoutes_js_1 = __importDefault(require("./routes/authRoutes.js"));
const fsWatcher_js_1 = __importDefault(require("./routes/fsWatcher.js"));
const firebase_js_1 = __importDefault(require("./firebase/firebase.js"));
const fs_1 = __importDefault(require("fs"));
const http_1 = __importDefault(require("http"));
const events_1 = __importDefault(require("events"));
var privateKey = fs_1.default.readFileSync('src/cert/server.key', 'utf8');
var certificate = fs_1.default.readFileSync('src/cert/server.crt', 'utf8');
const credentials = { key: privateKey, cert: certificate };
const detectionserver_js_1 = __importDefault(require("./detectionserver.js"));
const rxjs_1 = require("rxjs");
const PORT = 6543;
const app = (0, express_1.default)();
const router = express_1.default.Router();
app.use(express_1.default.json());
app.use(express_1.default.urlencoded({ extended: true }));
app.use(downloadRoutes_js_1.default);
app.use(directoryRoutes_js_1.default);
app.use(clipboardRoutes_js_1.default);
app.use(browserRoutes_js_1.default);
app.use(statusRoutes_js_1.default);
app.use(osRoutes_js_1.default);
app.use(fileOperationsRoutes_js_1.default);
app.use(uploadRoutes_js_1.default);
app.use(authRoutes_js_1.default);
app.use(router);
let detectionServerConfiguration = new rxjs_1.BehaviorSubject({ portNumber: 4285 });
let detectionServer = new detectionserver_js_1.default(detectionServerConfiguration);
exports.events = new events_1.default();
let https_server = null;
let http_server = null;
function startServer() {
    detectionServer.run();
    (0, firebase_js_1.default)();
    http_server = http_1.default.createServer(app).listen(6544);
    https_server = httpsServer.listen(PORT);
    exports.events.emit('server-started');
}
exports.startServer = startServer;
function stopServer() {
    if (https_server != null) {
        detectionServer.close();
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
const httpsServer = https_1.default.createServer(credentials, app);
``;
httpsServer.on('upgrade', (request, socket, head) => {
    fsWatcher_js_1.default.handleUpgrade(request, socket, head, socket => {
        fsWatcher_js_1.default.emit('connection', socket, request);
    });
});
startServer();
setTimeout(() => {
    detectionServer.close();
    setTimeout(detectionServer.run, 3000);
}, 3000);
