

class Response {

    result: string;

    constructor(isSuccess: boolean) {
        this.result = isSuccess ? "ok" : "error"
    }

}


class ErrorResponse extends Response {

    code: number;
    message: string;

    constructor(code, message) {
        super(false);
        this.code = code;
        this.message = message;
    }

}


class SuccessResponse extends Response {

    data: object | undefined = undefined;

    constructor(data = undefined) {
        super(true);
        if (data != undefined)
            this.data = data;
    }

}


module.exports = {ErrorResponse, SuccessResponse}