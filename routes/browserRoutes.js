const router = require('express').Router();
const open = require('open');
const { SuccessResponse, ErrorResponse } = require('../model/response');



function isValidURL(url) {
    try {
        url = new URL(url)
    } catch (e) {
        return false;
    }
    return url.protocol == 'http:' || url.protocol == 'https:'
}


router.post('/openLink', (req, res) => {

    const url = req.body.url;
    const incognito = req.body.incognito;

    if (isValidURL(url)) {
        if (incognito == true)
            open(url, {app: {name: 'msedge', arguments: ['-inPrivate']}});
        else
            open(url);

        res.json(new SuccessResponse());
    } else {
        res.json(new ErrorResponse(10, "Invalid http URL"));
    }

});


module.exports = router;