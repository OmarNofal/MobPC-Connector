

/**
 * A response object simplifies the way we respond to 
 * `http` requeusts, it is used directly as a json response to the user
 * like this `res.json(Response)`
 */
class Response {

    result: string;

    constructor(isSuccess: boolean) {
        this.result = isSuccess ? "ok" : "error"
    }

}

/**
 * Subclass of {@link Response} that suggests that an error occured
 * while performing the requested operation. Contains a code identifying the error
 * and a message explaining the error
 */
export class ErrorResponse extends Response {

    /** Code identifying the error */
    code: number
    /** Message summarizing the error*/
    message: string

    constructor(code, message) {
        super(false)
        this.code = code
        this.message = message
    }
    

}

/**
 * Subclass of {@link Response} that indiactes that the request was
 * completed successfuly and optionally returns some type
 */
export class SuccessResponse extends Response {

    /** optional data to return to the client */
    data?: object

    constructor(data = undefined) {
        super(true)
        if (data != undefined)
            this.data = data
    }

}