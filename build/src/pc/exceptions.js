"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UnsupportedOperationException = void 0;
class UnsupportedOperationException extends Error {
    constructor(message = "Not supported") {
        super(message);
    }
}
exports.UnsupportedOperationException = UnsupportedOperationException;
