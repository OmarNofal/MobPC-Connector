const { ErrorResponse, SuccessResponse } = require('../model/response');

const router = require('express').Router();
const { logInAndGetAccessToken, isLoggedIn } = require('../auth/auth');
const {} = require('../auth/exceptions');


router.post('/login', function(req, res) {

    const body = req.body;

    const password = body.password;

    if (!password) {
        res.statusCode = 400;
        res.json(new ErrorResponse(10, "Password field not set"));
        return;
    }

    try {
        const token = logInAndGetAccessToken(password);
        res.json(new SuccessResponse({token: token}));
    } catch (e) {
        res.json(new ErrorResponse(10, "Wrong Password"));
        return;
    }

})

router.get('/verifyToken', function(req, res) {
    const params = req.query;
    const token = params.token;

    if (!token) {
         res.json(new ErrorResponse(1, "Missing TOKEN"));
    } else {
        if (isLoggedIn(token)) {
            res.json(new SuccessResponse({valid: true}));    
        } else {
            res.json(new SuccessResponse({valid: false}));
        }
    }
})

module.exports = router;