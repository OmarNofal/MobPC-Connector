const router = require('express').Router();
const clipboardy = require('clipboardy');
const { notifyOfNewClipboardItem } = require('../utilities/notificationSystem')

const { SuccessResponse, ErrorResponse } = require('../model/response');




router.post('/copyToClipboard', (req, res) => {

    const text = req.body.text;

    if (text == undefined)
        res.json(new ErrorResponse(11, "Missing [text] field"));

    if (typeof text !== "string") {
        res.json(new ErrorResponse(12, "[text] must be a string"));
    }

    clipboardy.write(text)
        .then(() => {
            res.json(new SuccessResponse());
            notifyOfNewClipboardItem(text);
        }).catch( err => {
            console.log(err);
            res.json(new ErrorResponse(14, err));
        });

});



module.exports = router;