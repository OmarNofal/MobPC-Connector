"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Directory = exports.File = void 0;
/**
 * Base class for Directory and File resources containing common
 * properties between them
 */
class Resource {
    constructor(name, size, creationDate, lastModificationDate) {
        this.name = name;
        this.size = size;
        this.creationDate = Math.floor(creationDate);
        this.lastModificationDate = Math.floor(lastModificationDate);
    }
}
/**Represents a file resource on a the system */
class File extends Resource {
    constructor(name, size, creationDate, lastModificationDate) {
        super(name, size, creationDate, lastModificationDate);
        this.type = 'file';
    }
}
exports.File = File;
class Directory extends Resource {
    constructor(name, size, creationDate, lastModificationDate, resources) {
        super(name, size, creationDate, lastModificationDate);
        this.content = resources;
        this.type = 'directory';
        this.numberOfResources = resources.length;
    }
}
exports.Directory = Directory;
