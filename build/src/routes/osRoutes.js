"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const response_1 = require("../model/response");
const exceptions_1 = require("../pc/exceptions");
const pcOperations_1 = require("../pc/pcOperations");
function lockPcController(_, res) {
    try {
        pcOperations_1.pcOps.lockPc();
    }
    catch (err) {
        if (err instanceof exceptions_1.UnsupportedOperationException) {
            return res.json(new response_1.ErrorResponse(199, "This feature is only available on Windows"));
        }
    }
    return res.json(new response_1.SuccessResponse());
}
function shutdownPcController(req, res) {
    res.json(new response_1.SuccessResponse()); // send response first
    pcOperations_1.pcOps.shutdownPc();
}
function sendNotificationController(req, res) {
    const body = req.body;
    const title = body.title;
    const message = body.message;
    const appName = body.appName;
    if (!title || !message || !appName) {
        res.json(new response_1.ErrorResponse(1, "One or more missing fields. Required [title] [message] [appName]"));
        return;
    }
    pcOperations_1.pcOps.sendNotification({
        appName: appName,
        title: title,
        text: message,
        iconPath: undefined
    });
    res.json(new response_1.SuccessResponse());
}
function addOSRoutes(app, authMiddleware) {
    app.get('/lockPc', authMiddleware, lockPcController);
    app.get('/shutdownPc', authMiddleware, shutdownPcController);
    app.post('/sendNotification', authMiddleware, sendNotificationController);
}
exports.default = addOSRoutes;
