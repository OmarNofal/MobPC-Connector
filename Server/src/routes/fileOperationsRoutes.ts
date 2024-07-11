import { getDirectoryStructure } from '../fs/directories'
import { ResourceAlreadyExists } from '../fs/exceptions'
import {
    copyResources,
    renameResource,
    moveResources,
    deleteResources,
    parsePath,
    getResourceInfo,
} from '../fs/operations'
import { Request, Response, Router } from 'express'
import { SuccessResponse, ErrorResponse } from '../model/response'
import path from 'path'
import getDrives from '../fs/drives'
import authMiddleware, { AuthMiddlewareFunction } from './authMiddleware'

const router = Router()

function copyResourcesController(req: Request, res: Response) {
    const body = req.body

    console.log(body)

    const resourcesPath = parsePath(body.src)
    const pasteDestination = parsePath(body.dest)
    const overwrite = body.overwrite ?? false

    if (!resourcesPath || !pasteDestination) {
        res.json(new ErrorResponse(10, 'Missing [src] or [dest] resources'))
        return
    }

    try {
        copyResources(
            resourcesPath,
            pasteDestination,
            (theFileData, overrallsize, theFiles) => {
                const progress = {
                    currentFile: theFileData.file,
                    totalFileBytesCopied: theFileData.totalBytesCopied,
                    totalFileSize: theFileData.totalSize,
                    totalSize: overrallsize.totalSize,
                    totalBytesCopied: overrallsize.totalBytesCopied,
                }
                //res.write(JSON.stringify(progress) + "\n");
            },
            (theFileData, overrallsize, theFiles) => {
                //res.write(JSON.stringify( new SuccessResponse() ));
                //res.end();
            },
            (theFileData, overallSize, theFiles) => {
                console.log(theFileData.err)
                //res.json(new ErrorResponse(1, theFileData.err))
            },
            overwrite
        )
        res.json(new SuccessResponse())
    } catch (err) {
        console.log(err)
        if (err instanceof ResourceAlreadyExists) {
            return res.json(new ErrorResponse(10, 'A resource already exists. Please enable the overwrite flag'))
        } else return res.json(new ErrorResponse(10, 'Uncaught error: ' + err))
    }
}

function moveResourcesController(req: Request, res: Response) {
    const body = req.body

    const resourcesPath = body.src
    const pasteDestination = body.dest
    const overwrite = body.overwrite ?? false

    if (!resourcesPath || !pasteDestination) {
        res.json(new ErrorResponse(10, 'Missing [src] or [dest] resources'))
        return
    }

    try {
        moveResources(
            resourcesPath,
            pasteDestination,
            (theFileData, overrallsize, theFiles) => {
                const progress = {
                    currentFile: theFileData.file,
                    totalFileBytesCopied: theFileData.totalBytesCopied,
                    totalFileSize: theFileData.totalSize,
                    totalSize: overrallsize.totalSize,
                    totalBytesCopied: overrallsize.totalBytesCopied,
                }
                res.write(JSON.stringify(progress) + '\n')
            },
            (theFileData, overrallsize, theFiles) => {
                res.write(JSON.stringify(new SuccessResponse()))
                res.end()
            },
            (theFileData, overallSize, theFiles) => {
                console.log(theFileData.err)
                res.json(new ErrorResponse(1, theFileData.err))
            },
            overwrite
        )
    } catch (err) {
        if (err instanceof ResourceAlreadyExists) {
            return res.json(new ErrorResponse(10, 'A resource already exists. Please enable the overwrite flag'))
        } else return res.json(new ErrorResponse(10, 'Uncaught error: ' + err))
    }
}

function deleteResourceController(req: Request, res: Response) {
    const body = req.body

    var src = body.src
    var permanentlyDelete = (body.permanentlyDelete ?? 0) == 1 ? true : false

    src = parsePath(src)

    deleteResources(
        src,
        permanentlyDelete,
        (_) => {},
        () => {
            res.json(new SuccessResponse())
        }
    )
}

function renameResourceController(req: Request, res: Response) {
    const body = req.body

    var src = body.src
    const newName = body.newName
    const overwrite = body.overwrite ?? false

    src = parsePath(src)

    console.log('Renaming ' + src + ' to ' + newName)

    try {
        renameResource(src, newName, overwrite)
    } catch (err) {
        if (err instanceof ResourceAlreadyExists) {
            res.json(
                new ErrorResponse(
                    10,
                    'This resource already exists. Enable the [overwrite] flag to overwrite but note that the old file will be lost'
                )
            )
            return
        } else {
            res.json(new ErrorResponse(10, 'Unknown error: ' + err))
            return
        }
    }
    res.json(new SuccessResponse())
}

function getDrivesController(req: Request, res: Response) {
    getDrives(
        (err) => {
            res.send(new ErrorResponse(1, 'Failed to retrieve drives'))
        },
        (drives) => {
            // return all drive paths
            const names = drives.flatMap((d) => d.mountpoints).map((d) => d.path)

            res.send(new SuccessResponse(names))
        }
    )
}

function getResourceInfoController(req: Request, res: Response) {
    const body = req.body

    var src = body.path
    src = parsePath(src)

    try {
        const result = getResourceInfo(src)
        console.log(JSON.stringify(result))
        return res.json(new SuccessResponse(result))
    } catch (err) {
        res.json(new ErrorResponse(0, err))
    }
}

export function addFileOperationsRoutes(app: Router, authMiddleware: AuthMiddlewareFunction) {
    app.post('/copyResources', authMiddleware, copyResourcesController)
    app.post('/moveResources', authMiddleware, moveResourcesController)
    app.post('/deleteResources', authMiddleware, deleteResourceController)
    app.post('/renameResource', authMiddleware, renameResourceController)
    app.post('/resourceInfo', authMiddleware, getResourceInfoController)
    app.get('/drives', authMiddleware, getDrivesController)
}
