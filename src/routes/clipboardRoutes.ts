import { Request, Response, Router } from "express";
import { ErrorResponse, SuccessResponse } from "../model/response";
import { clipboardService } from "../service/clipboardService";


function copyToClipboardController(req: Request, res: Response) {
    const text: string | undefined = req.body.text;

    if (text == undefined)
        res.json(new ErrorResponse(11, "Missing [text] field"));

    if (typeof text !== "string") {
        res.json(new ErrorResponse(12, "[text] must be a string"));
    }

    clipboardService.writeTextToClipboard(text)

    return res.json(new SuccessResponse())
}

export default function addClipboardRoutes(
    app: Router,
    authMiddleware: (req: Request, res: Response, next: () => void) => void
) {
    app.post('/copyToClipboard', authMiddleware, copyToClipboardController)
}