import { Request, Response, Router } from 'express'
import fs from 'fs'
import { DaemonicProgress } from 'fsprogress'
import multer from 'multer'
import path from 'path'
import { parsePath } from '../fs/operations'
import { ErrorResponse, SuccessResponse } from '../model/response'
import { AuthMiddlewareFunction } from './authMiddleware'

function uploadFilesController(req: Request, res: Response) {
    const body = req.body

    const destination = parsePath(body.dest)

    console.log(destination)
    console.log(req.files)

    if (!destination || req.files.length == 0) {
        res.json(new ErrorResponse(10, 'Missing files or destination path'))
        return
    }

    const files = req.files as Express.Multer.File[]

    console.log(req.files.length)
    for (const file of files) {
        console.log(file)

        var directory = file.fieldname
        const fileDestination = path.join(destination, directory)

        var fileName = file.originalname
        const currentPath = file.path

        while (fs.existsSync(path.join(fileDestination, fileName))) {
            fileName = path.parse(fileName).name + '(1)' + path.parse(fileName).ext
        }

        try {
            const fileTempDirectory = path.join(file.path, '../', directory)
            fs.mkdirSync(fileTempDirectory, { recursive: true })

            fs.renameSync(currentPath, path.join(fileTempDirectory, fileName))

            if (!fs.existsSync(fileDestination)) {
                fs.mkdirSync(fileDestination, { recursive: true })
            }

            new DaemonicProgress(path.join(fileTempDirectory, fileName), fileDestination, { mode: 'move' }).start()
        } catch (e) {
            console.log(e)
        }
    }

    res.json(new SuccessResponse())
}

export default function addUploadRoutes(
    app: Router,
    uploadTempDestination: string,
    authMiddleware: AuthMiddlewareFunction
) {
    const upload = multer({ dest: uploadTempDestination })
    app.post('/uploadFiles', authMiddleware, upload.any(), uploadFilesController)
}
