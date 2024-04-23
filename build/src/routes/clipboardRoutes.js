"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const clipboardy_1 = __importDefault(require("clipboardy"));
const notificationSystem_1 = require("../utilities/notificationSystem");
const response_1 = require("../model/response");
const authMiddleware_1 = __importDefault(require("./authMiddleware"));
const router = (0, express_1.Router)();
router.post('/copyToClipboard', authMiddleware_1.default, (req, res) => {
    const text = req.body.text;
    if (text == undefined)
        res.json(new response_1.ErrorResponse(11, "Missing [text] field"));
    if (typeof text !== "string") {
        res.json(new response_1.ErrorResponse(12, "[text] must be a string"));
    }
    clipboardy_1.default.write(text)
        .then(() => {
        res.json(new response_1.SuccessResponse());
        (0, notificationSystem_1.notifyOfNewClipboardItem)(text);
    }).catch(err => {
        console.log(err);
        res.json(new response_1.ErrorResponse(14, err));
    });
});
exports.default = router;
