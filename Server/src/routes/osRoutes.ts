import { Request, Response, Router } from 'express'
import { ErrorResponse, SuccessResponse } from '../model/response'
import { UnsupportedOperationException } from '../service/pcOperations/exceptions'
import { pcOps } from '../service/pcOperations/pcOperations'
import { AuthMiddlewareFunction } from './authMiddleware'
import { notificationService } from '../service/notificationService'

function lockPcController(_: Request, res: Response) {
    try {
        pcOps.lockPc()
    } catch (err) {
        if (err instanceof UnsupportedOperationException) {
            return res.json(new ErrorResponse(199, 'This feature is only available on Windows'))
        }
    }

    return res.json(new SuccessResponse())
}

function shutdownPcController(req: Request, res: Response) {
    res.json(new SuccessResponse()) // send response first
    pcOps.shutdownPc()
}

function sendNotificationController(req: Request, res: Response) {
    const body = req.body

    const title = body.title
    const text = body.text
    const appName = body.appName
    const icon = body.icon

    if (!title || !text || !appName) {
        res.json(new ErrorResponse(1, 'One or more missing fields. Required [title] [text] [appName]'))
        return
    }

    let iconBuffer = null
    if (icon) {
        try {
            iconBuffer = Buffer.from(icon, 'base64')
        } catch (e) {
            console.error('Failed to decode icon: ' + e)
        }
    }

    notificationService.postNotification({
        title,
        text,
        appName,
        icon: iconBuffer,
    })

    res.json(new SuccessResponse())
}

export default function addOSRoutes(app: Router, authMiddleware: AuthMiddlewareFunction) {
    app.get('/lockPc', authMiddleware, lockPcController)
    app.get('/shutdownPc', authMiddleware, shutdownPcController)
    app.post('/sendNotification', authMiddleware, sendNotificationController)
}
