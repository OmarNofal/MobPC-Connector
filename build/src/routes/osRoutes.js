"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const response_1 = require("../model/response");
const pcOperations_1 = require("../pc/pcOperations");
const exceptions_1 = require("../pc/exceptions");
const authMiddleware_1 = __importDefault(require("./authMiddleware"));
const router = (0, express_1.Router)();
router.get('/lockPc', authMiddleware_1.default, (_, res) => {
    try {
        pcOperations_1.pcOps.lockPc();
    }
    catch (err) {
        if (err instanceof exceptions_1.UnsupportedOperationException) {
            return res.json(new response_1.ErrorResponse(199, "This feature is only available on Windows"));
        }
    }
    return res.json(new response_1.SuccessResponse());
});
router.get('/shutdownPc', authMiddleware_1.default, (_, res) => {
    res.json(new response_1.SuccessResponse()); // send response first
    pcOperations_1.pcOps.shutdownPc();
});
router.post('/sendNotification', authMiddleware_1.default, (req, res) => {
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
});
exports.default = router;
