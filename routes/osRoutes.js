const router = require('express').Router();
const { SuccessResponse, ErrorResponse } = require('../model/response');
const {lockPc, shutdownPc, sendNotification} = require('../pc/pcOperations');
const {UnsupportedOperationException: UnsupportedOSException} = require('../pc/exceptions');
const authMiddleware = require('./authMiddleware');



router.get('/lockPc', authMiddleware, (req, res) => {

    try {
        lockPc();
    } catch (err) {
        if (err instanceof UnsupportedOSException) {
            return res.json(new ErrorResponse(199, "This feature is only available on Windows"));
        }
    }

    return res.json(new SuccessResponse());
});


router.get('/shutdownPc', authMiddleware, (req, res) => {
    res.json(new SuccessResponse());
    shutdownPc();
});

router.post('/sendNotification', authMiddleware, (req, res) => {
    const body = req.body;

    const title = body.title;
    const message = body.message;
    const appName = body.appName;

    if (!title || !message || !appName) {
        res.json(new ErrorResponse(1, "One or more missing fields. Required [title] [message] [appName]"))
        return;
    }

    sendNotification(appName, title, message, body.iconPath);
    res.json(new SuccessResponse());
})

module.exports = router;