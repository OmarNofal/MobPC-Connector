"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const response_1 = require("../model/response");
function loginController(req, res, authManager) {
    const body = req.body;
    const password = body.password;
    if (!password) {
        res.statusCode = 400;
        res.json(new response_1.ErrorResponse(10, "Password field not set"));
        console.log("Password not set");
        return;
    }
    try {
        const token = authManager.logInAndGetAccessToken(password);
        res.json(new response_1.SuccessResponse({ token: token }));
    }
    catch (e) {
        console.log(e);
        console.log("Wrong password");
        res.json(new response_1.ErrorResponse(10, "Wrong Password"));
        return;
    }
}
function verifyTokenController(req, res, authManager) {
    const params = req.query;
    const token = params.token;
    if (!token || typeof token != "string") {
        res.json(new response_1.ErrorResponse(1, "Missing TOKEN"));
    }
    else {
        if (authManager.isValidToken(token)) {
            res.json(new response_1.SuccessResponse({ valid: true }));
        }
        else {
            res.json(new response_1.SuccessResponse({ valid: false }));
        }
    }
}
function addAuthRoutes(app, authManager) {
    app.post('/login', (req, res) => loginController(req, res, authManager));
    app.get('/verifyToken', (req, res) => verifyTokenController(req, res, authManager));
}
exports.default = addAuthRoutes;
