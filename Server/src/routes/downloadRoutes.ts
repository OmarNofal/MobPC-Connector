import CombinedStream from 'combined-stream'
import { Request, Response, Router } from 'express'
import fs from 'fs'
import jwt from 'jsonwebtoken'
import mime from 'mime'
import path from 'path'
import { env } from 'process'
import { parsePath } from '../fs/operations'
import { ErrorResponse, SuccessResponse } from '../model/response'
import { AuthMiddlewareFunction } from './authMiddleware'
require('dotenv').config()

function handleDownloadFile(file_path: string, res: Response) {
    console.log('Downloading: ' + file_path)

    const fileName = path.basename(file_path)
    res.set('Content-Disposition', `inline; filename="${fileName}"`)
    res.sendFile(file_path)
}

function handleDownloadFolder(src: string, res: Response) {
    const responseHeader = {
        numberOfFiles: 0,
        totalSize: 0,
        files: [],
    }

    const responseStream = new CombinedStream()

    const dirQueue = [src]
    while (dirQueue.length > 0) {
        const currentDirectory = dirQueue.shift()

        const contents = fs.readdirSync(currentDirectory, { withFileTypes: true })

        for (const file of contents) {
            const filePath = path.join(currentDirectory, file.name)

            if (file.isDirectory()) dirQueue.push(filePath)
            else if (file.isFile()) {
                const fileStats = fs.statSync(filePath)

                responseHeader.numberOfFiles++
                responseHeader.totalSize += fileStats.size
                responseHeader.files.push({
                    name: file.name,
                    size: fileStats.size,
                    path: path.relative(src, currentDirectory),
                    mimeType: mime.lookup(file.name),
                })

                responseStream.append(fs.createReadStream(filePath))
            }
        }
    }
    console.log(responseHeader)
    res.setHeader('Content-Type', 'folder')

    const header = JSON.stringify(responseHeader)
    const headerSizeBuffer = Buffer.allocUnsafe(8)
    headerSizeBuffer.writeBigInt64BE(BigInt(header.length), 0)
    res.write(headerSizeBuffer)
    res.write(header)

    responseStream.pipe(res)
}

function downloadFilesController(req: Request, res: Response) {
    const body = req.body

    var src = body.src ?? req.query.src
    src = parsePath(src)

    if (!src) {
        return res.json(new ErrorResponse(109, 'Missing [src] field'))
    }

    if (fs.existsSync(src)) {
        const srcStats = fs.statSync(src)
        if (srcStats.isFile()) {
            return handleDownloadFile(src, res)
        } else if (srcStats.isDirectory()) {
            return handleDownloadFolder(src, res)
        }
    } else {
        res.json(new ErrorResponse(1, 'This resource does not exist'))
    }
}

function getFileAccessTokenController(req: Request, res: Response, generateFileAccessTokenFn: (p) => string) {
    const path = req.query.src

    if (typeof path != 'string') {
        return
    }

    if (fs.existsSync(path)) {
        const token = generateFileAccessTokenFn(path)
        return res.json(new SuccessResponse({ token: token }))
    } else {
        return res.json(new ErrorResponse(1, 'File does not exist'))
    }
}

function getFileExternalController(req: Request, res: Response, getPathFromFileAccessToken: (token) => string | undefined) {
    const token = req.query.token
    const userPath = req.query.path

    if (!userPath || !token || typeof userPath != 'string' || typeof token != 'string') {
        return res.json(new ErrorResponse(2, 'Missing token or file path'))
    }

    
    const tokenPath = getPathFromFileAccessToken(token)

    if (tokenPath != undefined && path.resolve(tokenPath) == path.resolve(userPath)) {
        handleDownloadFile(tokenPath, res)
    } else if (tokenPath == undefined) {
        res.status(401)
        return res.json(new ErrorResponse(3, 'Invalid token'))
    } else {
        res.status(401)
        return res.json(new ErrorResponse(3, 'Path does not match the token'))
    }
}

export default function addDownloadRoutes(
    app: Router,
    authMiddleware: AuthMiddlewareFunction,
    generateFileAccessTokenFn: (path: string) => string,
    getPathFromFileAccessToken: (token: string) => string
) {
    app.get('/downloadFiles', authMiddleware, downloadFilesController)
    app.get('/getFileAccessToken', authMiddleware, (req, res) => getFileAccessTokenController(req, res, generateFileAccessTokenFn))
    app.get('/download/*', (req, res) => getFileExternalController(req, res, getPathFromFileAccessToken))
}
