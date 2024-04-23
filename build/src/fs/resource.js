"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class Resource {
    constructor(name, size, creationDate, lastModificationDate) {
        this.name = name;
        this.size = size;
        this.creationDate = Math.floor(creationDate);
        this.lastModificationDate = Math.floor(lastModificationDate);
    }
}
class File extends Resource {
    constructor(name, size, creationDate, lastModificationDate) {
        super(name, size, creationDate, lastModificationDate);
        this.type = 'file';
    }
}
class Directory extends Resource {
    constructor(name, size, creationDate, lastModificationDate, resources) {
        super(name, size, creationDate, lastModificationDate);
        this.content = resources;
        this.type = 'directory';
        this.numberOfResources = resources.length;
    }
}
module.exports = { File, Directory };
