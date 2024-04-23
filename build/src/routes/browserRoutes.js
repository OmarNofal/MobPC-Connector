"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const open_1 = __importDefault(require("open"));
const response_1 = require("../model/response");
const authMiddleware_1 = __importDefault(require("./authMiddleware"));
const router = (0, express_1.Router)();
function isValidURL(url) {
    try {
        url = new URL(url);
    }
    catch (e) {
        return false;
    }
    return url.protocol == 'http:' || url.protocol == 'https:';
}
router.post('/openLink', authMiddleware_1.default, (req, res) => {
    const url = req.body.url;
    const incognito = req.body.incognito;
    if (isValidURL(url)) {
        if (parseInt(incognito))
            (0, open_1.default)(url, { app: { name: 'msedge', arguments: ['-inPrivate'] } });
        else
            (0, open_1.default)(url);
        res.json(new response_1.SuccessResponse());
    }
    else {
        res.json(new response_1.ErrorResponse(10, "Invalid http URL"));
    }
});
exports.default = router;
