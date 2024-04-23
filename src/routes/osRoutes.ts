import { Router } from 'express';
import { SuccessResponse, ErrorResponse } from '../model/response';
import { pcOps } from '../pc/pcOperations';
import { UnsupportedOperationException } from '../pc/exceptions';
import authMiddleware from './authMiddleware'


const router = Router()

router.get('/lockPc', authMiddleware, (_, res) => {

    try {
        pcOps.lockPc()
    } catch (err) {
        if (err instanceof UnsupportedOperationException) {
            return res.json(new ErrorResponse(199, "This feature is only available on Windows"))
        }
    }
    
    return res.json(new SuccessResponse());
})


router.get('/shutdownPc', authMiddleware, (_, res) => {
    res.json(new SuccessResponse()) // send response first
    pcOps.shutdownPc()
})

router.post('/sendNotification', authMiddleware, (req, res) => {
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
})

export default router