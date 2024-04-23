"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const storage_1 = require("../storage");
function authMiddleware(req, res, next) {
    const authHeader = req.headers['authorization'];
    if (!authHeader) {
        // If no Authorization header is present, return an error response
        return res.status(401).json({ error: 'Authorization header missing' });
    }
    // The Authorization header usually looks like: "Bearer <token>"
    const token = authHeader.split(' ')[1]; // Get the token part after 'Bearer'
    if (!token) {
        // If no token is found, return an error response
        return res.status(401).json({ error: 'No token provided' });
    }
    if ((0, storage_1.isLoggedIn)(token)) {
        next();
    }
}
exports.default = authMiddleware;
