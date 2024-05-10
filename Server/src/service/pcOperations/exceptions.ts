
export class UnsupportedOperationException extends Error {

    constructor(message = "Not supported") {
        super(message);
    }

}