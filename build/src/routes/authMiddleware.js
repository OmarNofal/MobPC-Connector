"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const response_1 = require("../model/response");
/**
 * Since the authMiddleware function depends on
 * the `AuthenticationManager` class being available,
 * we need to create the function when we launch the app
 * and create the `AuthenticationManager` object
 */
function createAuthMiddlewareFunction(isLoggedIn) {
    return (req, res, next) => {
        const authHeader = req.headers['authorization'];
        if (!authHeader) {
            return res.status(401).json({ error: 'Authorization header missing' });
        }
        // The Authorization header usually looks like: "Bearer <token>"
        const token = authHeader.split(' ')[1]; // Get the token part after 'Bearer'
        if (!token) {
            return res.status(401).json({ error: 'No token provided' });
        }
        if (isLoggedIn(token)) {
            next();
        }
        else {
            return res.status(401).json(new response_1.ErrorResponse(34, "Expired token"));
        }
    };
}
exports.default = createAuthMiddlewareFunction;
