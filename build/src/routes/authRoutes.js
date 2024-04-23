"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const response_1 = require("../model/response");
const express_1 = require("express");
const storage_1 = require("../storage");
const router = (0, express_1.Router)();
router.post('/login', function (req, res) {
    const body = req.body;
    const password = body.password;
    if (!password) {
        res.statusCode = 400;
        res.json(new response_1.ErrorResponse(10, "Password field not set"));
        console.log("Password not set");
        return;
    }
    try {
        const token = (0, storage_1.logInAndGetAccessToken)(password);
        res.json(new response_1.SuccessResponse({ token: token }));
    }
    catch (e) {
        console.log(e);
        console.log("Wrong password");
        res.json(new response_1.ErrorResponse(10, "Wrong Password"));
        return;
    }
});
router.get('/verifyToken', function (req, res) {
    const params = req.query;
    const token = params.token;
    if (!token) {
        res.json(new response_1.ErrorResponse(1, "Missing TOKEN"));
    }
    else {
        if ((0, storage_1.isLoggedIn)(token)) {
            res.json(new response_1.SuccessResponse({ valid: true }));
        }
        else {
            res.json(new response_1.SuccessResponse({ valid: false }));
        }
    }
});
exports.default = router;
