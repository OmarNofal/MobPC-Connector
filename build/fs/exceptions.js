"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class ResourceAlreadyExists extends Error {
    constructor() {
        super("This resource already exists");
    }
}
module.exports = { ResourceAlreadyExists };
