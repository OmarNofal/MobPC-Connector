import { Request, Response, Router } from 'express';
import { ErrorResponse, SuccessResponse } from '../model/response';
import { UnsupportedOperationException } from '../pc/exceptions';
import { pcOps } from '../pc/pcOperations';
import { AuthMiddlewareFunction } from './authMiddleware';


function lockPcController(_: Request, res: Response) {
    try {
        pcOps.lockPc()
    } catch (err) {
        if (err instanceof UnsupportedOperationException) {
            return res.json(new ErrorResponse(199, "This feature is only available on Windows"))
        }
    }
    
    return res.json(new SuccessResponse());
}

function shutdownPcController(req: Request, res: Response) {
    res.json(new SuccessResponse()) // send response first
    pcOps.shutdownPc()
}

function sendNotificationController(req: Request, res: Response) {
    const body = req.body

    const title = body.title
    const message = body.message
    const appName = body.appName

    if (!title || !message || !appName) {
        res.json(new ErrorResponse(1, "One or more missing fields. Required [title] [message] [appName]"))
        return
    }

    pcOps.sendNotification({
        appName: appName,
        title: title,
        text: message,
        iconPath: undefined
    })

    res.json(new SuccessResponse())
}

export default function addOSRoutes(
    app: Router, 
    authMiddleware: AuthMiddlewareFunction
) {
    app.get('/lockPc', authMiddleware, lockPcController)
    app.get('/shutdownPc', authMiddleware, shutdownPcController)
    app.post('/sendNotification', authMiddleware, sendNotificationController)
}