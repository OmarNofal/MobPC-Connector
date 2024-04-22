



class ResourceAlreadyExists extends Error {

    constructor() {
        super("This resource already exists");
    }
}


module.exports = {ResourceAlreadyExists};