"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const response_1 = require("../model/response");
const clipboardService_1 = require("../service/clipboardService");
function copyToClipboardController(req, res) {
    const text = req.body.text;
    if (text == undefined)
        res.json(new response_1.ErrorResponse(11, "Missing [text] field"));
    if (typeof text !== "string") {
        res.json(new response_1.ErrorResponse(12, "[text] must be a string"));
    }
    clipboardService_1.clipboardService.writeTextToClipboard(text);
    return res.json(new response_1.SuccessResponse());
}
function addClipboardRoutes(app, authMiddleware) {
    app.post('/copyToClipboard', authMiddleware, copyToClipboardController);
}
exports.default = addClipboardRoutes;
