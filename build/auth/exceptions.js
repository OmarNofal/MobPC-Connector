"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class InvalidPasswordException extends Error {
    constructor(message) {
        super(message);
        this.name = 'Invalid password';
    }
}
class PasswordNotSetException extends Error {
    constructor(message) {
        super(message);
        this.name = 'Invalid password';
    }
}
module.exports = { InvalidPasswordException, PasswordNotSetException };
