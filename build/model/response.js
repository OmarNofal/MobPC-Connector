"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class Response {
    constructor(isSuccess) {
        this.result = isSuccess ? "ok" : "error";
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
        this.data = undefined;
        if (data != undefined)
            this.data = data;
    }
}
module.exports = { ErrorResponse, SuccessResponse };
