import { Request, Response, Router } from "express";
import AuthenticationManager from "../auth/auth";
import { ErrorResponse, SuccessResponse } from "../model/response";


type LoginData = {
    password?: string
}

function loginController(req: Request, res: Response, authManager: AuthenticationManager) {
    const body: LoginData = req.body;

    const password: string | undefined = body.password;

    if (!password) {
        res.statusCode = 400
        res.json(new ErrorResponse(10, "Password field not set"))
        console.log("Password not set")
        return
    }

    try {
        const token = authManager.logInAndGetAccessToken(password)
        res.json(new SuccessResponse({ token: token }))
    } catch (e) {
        console.log(e)
        console.log("Wrong password")
        res.json(new ErrorResponse(10, "Wrong Password"))
        return
    }
}

function verifyTokenController(req: Request, res: Response, authManager: AuthenticationManager) {
    const params = req.query
    const token = params.token

    if (!token || typeof token != "string") {
        res.json(new ErrorResponse(1, "Missing TOKEN"))
    } else {
        if (authManager.isValidToken(token)) {
            res.json(new SuccessResponse({ valid: true }))
        } else {
            res.json(new SuccessResponse({ valid: false }))
        }
    }
}


export default function addAuthRoutes(app: Router, authManager: AuthenticationManager) {
    app.post('/login', (req, res) => loginController(req, res, authManager))
    app.get('/verifyToken', (req, res) => verifyTokenController(req, res, authManager))
}