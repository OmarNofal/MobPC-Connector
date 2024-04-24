"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const response_1 = require("../model/response");
const browserService_1 = require("../service/browserService");
function openLinkController(req, res) {
    const url = req.body.url;
    const incognito = parseInt(req.body.incognito) ? true : false;
    const result = browserService_1.browserService.openURLInBrowser(url, incognito);
    if (result) {
        res.json(new response_1.SuccessResponse());
    }
    else {
        res.json(new response_1.ErrorResponse(10, "Invalid http URL"));
    }
}
function addBrowserRoutes(app, authMiddleware) {
    app.post('/openLink', authMiddleware, openLinkController);
}
exports.default = addBrowserRoutes;
