"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.PasswordNotSetException = exports.InvalidPasswordException = void 0;
class InvalidPasswordException extends Error {
    constructor(message) {
        super(message);
        this.name = 'Invalid password';
    }
}
exports.InvalidPasswordException = InvalidPasswordException;
class PasswordNotSetException extends Error {
    constructor(message) {
        super(message);
        this.name = 'Invalid password';
    }
}
exports.PasswordNotSetException = PasswordNotSetException;
