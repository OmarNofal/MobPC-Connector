import { SuccessResponse, ErrorResponse } from "../model/response";
import { Router } from "express";
import { logInAndGetAccessToken, isLoggedIn } from "../storage";


const router = Router();


type LoginData = {
    password?: string
}

router.post('/login', function(req, res) {

    const body: LoginData = req.body;

    const password: string | undefined = body.password;

    if (!password) {
        res.statusCode = 400
        res.json(new ErrorResponse(10, "Password field not set"))
        console.log("Password not set")
        return
    }

    try {
        const token = logInAndGetAccessToken(password)
        res.json(new SuccessResponse({token: token}))
    } catch (e) {
        console.log(e)
        console.log("Wrong password")
        res.json(new ErrorResponse(10, "Wrong Password"))
        return
    }

})

router.get('/verifyToken', function(req, res) {

    const params = req.query
    const token = params.token

    if (!token) {
         res.json(new ErrorResponse(1, "Missing TOKEN"))
    } else {
        if (isLoggedIn(token)) {
            res.json(new SuccessResponse({valid: true}))
        } else {
            res.json(new SuccessResponse({valid: false}))
        }
    }
})

export default router