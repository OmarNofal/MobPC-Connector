import { Router } from 'express' 
import open from "open"
import { SuccessResponse, ErrorResponse } from "../model/response"
import authMiddleware from "./authMiddleware"

const router = Router()

function isValidURL(url) {
    try {
        url = new URL(url)
    } catch (e) {
        return false;
    }
    return url.protocol == 'http:' || url.protocol == 'https:'
}


router.post('/openLink', authMiddleware, (req, res) => {

    
    const url = req.body.url;
    const incognito = req.body.incognito;


    if (isValidURL(url)) {
        if (parseInt(incognito))
            open(url, {app: {name: 'msedge', arguments: ['-inPrivate']}});
        else
            open(url);

        res.json(new SuccessResponse());
    } else {
        res.json(new ErrorResponse(10, "Invalid http URL"));
    }

});


export default router;