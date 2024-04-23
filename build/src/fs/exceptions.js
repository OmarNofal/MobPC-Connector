"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ResourceAlreadyExists = void 0;
class ResourceAlreadyExists extends Error {
    constructor() {
        super("This resource already exists");
    }
}
exports.ResourceAlreadyExists = ResourceAlreadyExists;
