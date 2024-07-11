package com.omar.pcconnector.operation.transfer.download

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.operation.Operation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Path


/**
 * Opens and returns an input stream
 * to a single network file on the server.
 *
 * Primarily used in the document provider to open
 * streams of files for other applications
 */
class StreamFileOperation(
    private val api: FileSystemOperations,
    private val pathOnServer: Path
): Operation<InputStream>() {

    override val name: String
        get() = "Stream File"

    override val operationDescription: String
        get() = "Streaming File $pathOnServer"

    override suspend fun start(): InputStream  = withContext(Dispatchers.IO){

        val res = api.download(pathOnServer.absolutePath)

        return@withContext res.body()!!.byteStream().buffered()
    }

}