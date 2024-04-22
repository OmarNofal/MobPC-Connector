"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class UnsupportedOperationException extends Error {
    constructor(message = "Not supported") {
        super(message);
    }
}
module.exports = { UnsupportedOperationException };
