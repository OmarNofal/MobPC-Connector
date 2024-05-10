import { Request, Response, Router } from 'express' 
import { SuccessResponse, ErrorResponse } from "../model/response"
import { browserService } from '../service/browserService';



function openLinkController(req: Request, res: Response) {
    
    const url = req.body.url;
    const incognito = parseInt(req.body.incognito) ? true : false;

    const result = browserService.openURLInBrowser(url, incognito)

    if (result) {
        res.json(new SuccessResponse());
    } else {
        res.json(new ErrorResponse(10, "Invalid http URL"));
    }

}

export default function addBrowserRoutes(app: Router, authMiddleware: (req, res, next) => void) {
    app.post('/openLink', authMiddleware, openLinkController)
}
