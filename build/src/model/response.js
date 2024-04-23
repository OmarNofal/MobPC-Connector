"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.SuccessResponse = exports.ErrorResponse = void 0;
/**
 * A response object simplifies the way we respond to
 * `http` requeusts, it is used directly as a json response to the user
 * like this `res.json(Response)`
 */
class Response {
    constructor(isSuccess) {
        this.result = isSuccess ? "ok" : "error";
    }
}
/**
 * Subclass of {@link Response} that suggests that an error occured
 * while performing the requested operation. Contains a code identifying the error
 * and a message explaining the error
 */
class ErrorResponse extends Response {
    constructor(code, message) {
        super(false);
        this.code = code;
        this.message = message;
    }
}
exports.ErrorResponse = ErrorResponse;
/**
 * Subclass of {@link Response} that indiactes that the request was
 * completed successfuly and optionally returns some type
 */
class SuccessResponse extends Response {
    constructor(data = undefined) {
        super(true);
        if (data != undefined)
            this.data = data;
    }
}
exports.SuccessResponse = SuccessResponse;
