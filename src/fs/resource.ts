

/**
 * Base class for Directory and File resources containing common
 * properties between them
 */
class Resource {

    /**Name of this resource */
    name: string
    
    /**Size in bytes */
    size: number
    
    /**Creation date timestamp */
    creationDate: number

    /**Last modification date timestamp */
    lastModificationDate: number

    constructor(
        name: string,
        size: number,
        creationDate: number,
        lastModificationDate: number
    ) {
        this.name = name
        this.size = size
        this.creationDate = Math.floor(creationDate)
        this.lastModificationDate = Math.floor(lastModificationDate)
    }

}


/**Represents a file resource on a the system */
export class File extends Resource {

    /**'file' */
    type: string

    constructor(
        name: any,
        size: any,
        creationDate: any,
        lastModificationDate: any
    ) {
        super(name, size, creationDate, lastModificationDate)
        this.type = 'file'
    }

}


export class Directory extends Resource {


    content: any

    /**'directory' */
    type: string

    /**Number of directories and files in this directory */
    numberOfResources: number

    constructor(
        name: any,
        size: any,
        creationDate: any,
        lastModificationDate: any,
        resources: string | any[]
    ) {
        super(name, size, creationDate, lastModificationDate)
        this.content = resources
        this.type = 'directory'
        this.numberOfResources = resources.length
    }

}

