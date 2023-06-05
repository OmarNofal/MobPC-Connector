package com.omar.pcconnector.operation

import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.api.getDataOrThrow
import com.omar.pcconnector.network.api.toDomainResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ListDirectoryOperation(
    private val api: FileSystemOperations,
    private val path: String
): Operation<List<Resource>>() {

    override val name: String
        get() = "List Directory"

    override val operationDescription: String
        get() = "Listing directory: $path"

    override suspend fun start(): List<Resource> {
        var result = listOf<Resource>()
        withContext(Dispatchers.IO) {
            result = api.getDirectoryStructure(path).execute().body().getDataOrThrow()!!.map { it.toDomainResource() }
        }
        return result
    }

    override suspend fun cancel() {
        return
    }
}




class RenameOperation(
    private val api: FileSystemOperations,
    private val resourcePath: String,
    private val newName: String,
    private val overwrite: Boolean = false
): Operation<Unit>() {

    override val name: String
        get() = "Rename Operation"
    override val operationDescription: String
        get() = "Rename $resourcePath to $newName"

    override suspend fun start() {
        api.renameResource(resourcePath, newName, if (overwrite) 1 else 0).execute()
            .body().getDataOrThrow()
    }

    override suspend fun cancel() {
        return
    }
}




class DeleteOperation(
    private val api: FileSystemOperations,
    private val resourcePath: String,
    private val permanentlyDelete: Boolean
): Operation<Unit>() {

    override val name: String
        get() = "Delete Operation"
    override val operationDescription: String
        get() = "Deleting $resourcePath"

    override suspend fun start() {
        api.deleteResource(resourcePath, if (permanentlyDelete) 1 else 0).execute()
            .body().getDataOrThrow()
    }

    override suspend fun cancel() {
        return
    }
}


class MakeDirectoriesOperation(
    private val api: FileSystemOperations,
    private val path: String,
    private val directoryName: String
): Operation<Unit>() {

    override val name: String
        get() = "Make Dirs"
    override val operationDescription: String
        get() = "Creating directory $directoryName in $path"

    override suspend fun start() {
        api.makeDirs(path, directoryName).execute()
            .body().getDataOrThrow()
    }

    override suspend fun cancel() {
        return
    }
}
