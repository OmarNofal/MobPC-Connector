import { Request, Response, Router } from 'express'
import AuthorizationManager from '../auth/auth'
import { ErrorResponse, SuccessResponse } from '../model/response'

type PairingData = {
    pairingToken?: string
    os: string
    modelName: string
}

function pairingController(req: Request, res: Response, authManager: AuthorizationManager) {
    const body: PairingData = req.body

    const pairingToken: string | undefined = body.pairingToken

    if (!pairingToken) {
        res.statusCode = 400
        res.json(new ErrorResponse(10, 'Pairing token field not set'))
        return
    }

    const isValidPairingToken = authManager.isValidPairingToken(pairingToken)

    if (isValidPairingToken) {
        const token = authManager.pairWithNewDevice({modelName: body.modelName, os: body.os})
        res.json(new SuccessResponse({ token: token }))
    } else {
        res.json(new ErrorResponse(10, 'Wrong Pairing Token'))
        return
    }
}

function verifyTokenController(req: Request, res: Response, authManager: AuthorizationManager) {
    const params = req.query
    const token = params.token

    if (!token || typeof token != 'string') {
        res.json(new ErrorResponse(1, 'Missing TOKEN'))
    } else {
        if (authManager.isValidDeviceToken(token)) {
            res.json(new SuccessResponse({ valid: true }))
        } else {
            res.json(new SuccessResponse({ valid: false }))
        }
    }
}

export default function addAuthRoutes(app: Router, authManager: AuthorizationManager) {
    app.post('/pair', (req, res) => pairingController(req, res, authManager))
    app.get('/verifyToken', (req, res) => verifyTokenController(req, res, authManager))
}
