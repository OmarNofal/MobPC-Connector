"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const router = require('express').Router();
const open = require('open');
const { SuccessResponse, ErrorResponse } = require('../model/response');
const authMiddleware = require('./authMiddleware');
function isValidURL(url) {
    try {
        url = new URL(url);
    }
    catch (e) {
        return false;
    }
    return url.protocol == 'http:' || url.protocol == 'https:';
}
router.post('/openLink', authMiddleware, (req, res) => {
    const url = req.body.url;
    const incognito = req.body.incognito;
    if (isValidURL(url)) {
        if (parseInt(incognito))
            open(url, { app: { name: 'msedge', arguments: ['-inPrivate'] } });
        else
            open(url);
        res.json(new SuccessResponse());
    }
    else {
        res.json(new ErrorResponse(10, "Invalid http URL"));
    }
});
module.exports = router;
