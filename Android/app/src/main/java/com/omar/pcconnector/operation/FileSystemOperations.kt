package com.omar.pcconnector.operation

import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.api.getDataOrThrow
import com.omar.pcconnector.network.api.toDomainResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.nio.file.Path


class ListDirectoryOperation(
    private val api: FileSystemOperations,
    private val path: Path
): Operation<List<Resource>>() {

    override val name: String
        get() = "List Directory"

    override val operationDescription: String
        get() = "Listing directory: $path"

    override suspend fun start(): List<Resource> {
        var result = listOf<Resource>()
        withContext(Dispatchers.IO) {
            result = api.getDirectoryStructure(path.absolutePath).execute().body().getDataOrThrow()!!.map { it.toDomainResource(path) }

        }
        return result
    }


}

class GetDrivesOperation(
    private val api: FileSystemOperations
): Operation<List<String>>() {

    override val name: String
        get() = "List Drives"

    override val operationDescription: String
        get() = "Listing Drives"

    override suspend fun start(): List<String> {
        var result: List<String>
        withContext(Dispatchers.IO) {
            result = api.getDrives().getDataOrThrow() ?: listOf()
        }
        return result
    }


}


class RenameOperation(
    private val api: FileSystemOperations,
    private val resourcePath: Path,
    private val newName: String,
    private val overwrite: Boolean = false
): Operation<Unit>() {

    override val name: String
        get() = "Rename Operation"
    override val operationDescription: String
        get() = "Rename $resourcePath to $newName"

    override suspend fun start() {
        api.renameResource(resourcePath.absolutePath, newName, if (overwrite) 1 else 0).execute()
            .body().getDataOrThrow()
    }


}




class DeleteOperation(
    private val api: FileSystemOperations,
    private val resourcePath: Path,
    private val permanentlyDelete: Boolean
): Operation<Unit>() {

    override val name: String
        get() = "Delete Operation"
    override val operationDescription: String
        get() = "Deleting $resourcePath"

    override suspend fun start() {
        api.deleteResource(resourcePath.absolutePath, if (permanentlyDelete) 1 else 0).execute()
            .body().getDataOrThrow()
    }


}


class MakeDirectoriesOperation(
    private val api: FileSystemOperations,
    private val path: Path,
    private val directoryName: String
): Operation<Unit>() {

    override val name: String
        get() = "Make Dirs"
    override val operationDescription: String
        get() = "Creating directory $directoryName in $path"

    override suspend fun start() {
        api.makeDirs(path.absolutePath, directoryName).execute()
            .body().getDataOrThrow()
    }
}


class CopyResourcesOperation(
    private val api: FileSystemOperations,
    private val src: Path,
    private val dest: Path,
    private val overwrite: Boolean = false
): Operation<Unit>() {

    override val name: String
        get() = "Copying Resources"
    override val operationDescription: String
        get() = "Copying ${src.absolutePath} to ${dest.absolutePath}"

    override suspend fun start() = withContext(Dispatchers.IO){
        api.copyResources(src.absolutePath, dest.absolutePath, if (overwrite) 1 else 0).execute()
            .body().getDataOrThrow()
        Unit
    }
}


class GetFileAccessToken(
    private val api: FileSystemOperations,
    private val src: Path
): Operation<String>() {

    override val name: String
        get() = "File Access Token"
    override val operationDescription: String
        get() = "Obtaining file access token"

    override suspend fun start() = withContext(Dispatchers.IO){
        api.getFileAccessToken(src.absolutePath).getDataOrThrow()?.token
            ?: throw IllegalArgumentException("Empty token")
    }
}


class GetResourceInfoOperation(
    private val api: FileSystemOperations,
    private val path: Path
): Operation<Resource>() {

    override val name: String
        get() = "Get Resource Information"
    override val operationDescription: String
        get() = "Get Resource Information for file: $path"

    override suspend fun start() = withContext(Dispatchers.IO){
        api.getResourceInfo(path.absolutePath).execute()
            .body().getDataOrThrow()?.toDomainResource(path) ?: throw IllegalStateException()
    }
}
