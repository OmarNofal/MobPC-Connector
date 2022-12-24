

class Response {

    constructor(isSuccess) {
        this.result = isSuccess ? "ok" : "error"
    }

}


class ErrorResponse extends Response {

    constructor(code, message) {
        super(false);
        this.code = code;
        this.message = message;
    }

}


class SuccessResponse extends Response {

    constructor(data = undefined) {
        super(true);
        if (data != undefined)
            this.data = data;
    }

}


module.exports = {ErrorResponse, SuccessResponse}