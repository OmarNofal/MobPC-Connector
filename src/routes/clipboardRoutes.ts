import { Router } from "express"
import clipboardy from "clipboardy"
import { notifyOfNewClipboardItem } from "../utilities/notificationSystem"
import { SuccessResponse, ErrorResponse } from "../model/response"
import authMiddleware from './authMiddleware'


const router = Router()

router.post('/copyToClipboard', authMiddleware, (req, res) => {

    const text: string | undefined = req.body.text;

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


export default router