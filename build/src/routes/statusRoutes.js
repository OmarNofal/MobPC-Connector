"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const os_1 = __importDefault(require("os"));
const package_json_1 = __importDefault(require("../../package.json"));
const appindentification_1 = require("../identification/appindentification");
function statusController(req, res) {
    res.json({
        name: package_json_1.default.serverName,
        version: package_json_1.default.version,
        port: req.socket.localPort,
        ip: req.socket.localAddress,
        id: (0, appindentification_1.getUUID)(),
        os: os_1.default.platform()
    });
}
function addStatusRoutes(app) {
    app.get('/status', statusController);
}
exports.default = addStatusRoutes;
