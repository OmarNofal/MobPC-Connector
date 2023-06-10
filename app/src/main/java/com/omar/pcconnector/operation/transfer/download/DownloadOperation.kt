package com.omar.pcconnector.operation.transfer.download

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import com.google.gson.JsonParser
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.exceptions.InvalidResponseException
import com.omar.pcconnector.operation.MonitoredOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.BufferedSource
import java.lang.Long.min
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.math.pow


sealed class DownloadOperationState {

    object Initializing : DownloadOperationState()

    sealed class Initialized(
        val numberOfFiles: Int,
        val totalBytes: Long,
        val downloadedBytes: Long,
        val fileNames: List<String>
    ) : DownloadOperationState() {

        class Downloading(
            numberOfFiles: Int,
            numberOfDownloadedFiles: Int,
            totalBytes: Long,
            downloadedBytes: Long,
            fileNames: List<String>,
            currentDownloadingFile: String
        ) : Initialized(numberOfFiles, totalBytes, downloadedBytes, fileNames)

        class Downloaded(
            numberOfFiles: Int,
            totalBytes: Long,
            fileNames: List<String>
        ) : Initialized(numberOfFiles, totalBytes, totalBytes, fileNames)


    }

    object Cancelled : DownloadOperationState()

}

/**
 * Suspends coroutine until the buffer is filled
 */
private suspend fun BufferedSource.fillBuffer(byteBuffer: ByteBuffer) {
    withContext(Dispatchers.IO) {
        while (byteBuffer.remaining() > 0) {
            this@fillBuffer.read(byteBuffer)
        }
    }
}

class DownloadOperation(
    private val api: FileSystemOperations,
    private val pathOnServer: Path,
    private val downloadPath: DocumentFile,
    private val contentResolver: ContentResolver
) : MonitoredOperation<DownloadOperationState, Unit>() {
    override val name: String
        get() = "Download Operation"
    override val operationDescription: String
        get() = "Downloading file(s)"

    private lateinit var response: retrofit2.Response<ResponseBody>

    init {
        if (!downloadPath.isDirectory) {
            throw IllegalArgumentException("Destination Path is not a directory")
        }
    }

    override suspend fun start() {

        response = api.download(pathOnServer.absolutePath).execute()

        val contentDisposition = response.headers().get("Content-Disposition")
        val contentType = response.headers().get("Content-Type")

        // If there is content disposition in the header then we are downloading a file
        // if we are not

        return if (contentDisposition != null) handleDownloadFile()
        else
            when (contentType) {
                "folder" -> handleDownloadFolder()
                else -> handleError()
            }
    }

    override suspend fun cancel() {
        _progress.value = DownloadOperationState.Cancelled
    }

    private suspend fun handleError() {

    }

    private suspend fun handleDownloadFile() = withContext(Dispatchers.IO) {
        val body = response.body()!!
        val fileName =
            response.headers().get("Content-Disposition")!!.substringAfter("filename=").removeSuffix("\"").removePrefix("\"")
        val fileSize = response.headers().get("Content-Length")!!.toLong()

        publishProgress(1, 0, fileSize, 0L, fileName, listOf(fileName))

        val outFileStream =
            contentResolver.openOutputStream(
                downloadPath.createFile(
                    "no_mime_type/no",
                    fileName
                )?.uri ?: throw Exception("Could not create file in directory")
            )
                ?.buffered(
                    (2.0).pow(22).toInt()
                )!! //File(downloadPath, fileName).apply { createNewFile() }.outputStream().buffered((2.0).pow(22).toInt()) // 4MB buffer to speed up
        val source = body.byteStream()

        try {
            val buffer = ByteArray(1024 * 256)
            var totalRead: Long = 0
            var read: Int
            while (source.read(buffer, 0, 1024 * 256).also { read = it } != -1) {
                totalRead += read
                outFileStream.write(buffer, 0, read)
                publishProgress(1, 0, fileSize, totalRead, fileName, listOf(fileName))
            }
            publishFinished(1, fileSize, listOf(fileName))
        } finally {
            outFileStream.flush()
            outFileStream.close()
            source.close()
        }
    }


    private suspend fun handleDownloadFolder() = withContext(Dispatchers.IO) {
        val body = response.body()!!.source()

        val headerLengthBuffer = ByteBuffer.allocate(8)
        body.fillBuffer(headerLengthBuffer)

        val headerLength: Long = headerLengthBuffer.order(ByteOrder.BIG_ENDIAN).getLong(0)
        val headerBuffer = ByteBuffer.allocate(headerLength.toInt())
        body.fillBuffer(headerBuffer)

        val headerString = String(headerBuffer.array(), Charset.forName("UTF-8"))
        val json = JsonParser.parseString(headerString).asJsonObject

        try {
            val numberOfFiles = json["numberOfFiles"].asInt
            val totalSize = json["totalSize"].asLong
            val files = json["files"].asJsonArray
            var totalRead = 0L

            val filesNames = files.map { it.asJsonObject.get("name")!!.asString }

            for (f in files) {
                val fileInfo = f.asJsonObject
                val fileName = fileInfo["name"]!!.asString
                val fileSize = fileInfo["size"]!!.asLong
                val filePath = fileInfo["path"]!!.asString

                val splitPath = filePath.split("\\").toMutableList().apply {
                    add(0, pathOnServer.fileName.toString())
                }

                val fileDir = splitPath.fold(
                    downloadPath
                ) { acc, folderName ->
                    if (folderName.isBlank()) return@fold acc
                    val file = acc.findFile(folderName)
                    if (file?.isDirectory == true) return@fold file
                    else return@fold acc.createDirectory(folderName) ?: throw Exception("Failed to create file")
                }

                val outputFileStream =
                    contentResolver.openOutputStream(fileDir.createFile("no_mime_type/no", fileName)!!.uri)!!
                        .buffered((2.0).pow(22).toInt())

                var read: Int
                var fileTotalRead = 0
                val buffer = ByteArray(2048)

                while (fileTotalRead < fileSize) {
                    read = body
                        .inputStream().read(buffer, 0, min(2048, fileSize - fileTotalRead).toInt())
                    fileTotalRead += read
                    outputFileStream.write(buffer, 0, read)
                    publishProgress(numberOfFiles, files.indexOf(f), totalSize, totalRead, fileName, filesNames)
                }

                outputFileStream.close()
            }

            publishFinished(numberOfFiles, totalSize, filesNames)

        } catch (n: NullPointerException) {
            throw InvalidResponseException("Invalid message from the server")
        } finally {
            body.close()
        }
    }

    private fun publishProgress(
        totalNumberOfFiles: Int,
        numberOfDownloadedFiles: Int,
        totalBytes: Long,
        downloadedBytes: Long,
        currentDownloadingFile: String,
        fileNames: List<String>
    ) {
        _progress.value = DownloadOperationState.Initialized.Downloading(
            totalNumberOfFiles,
            numberOfDownloadedFiles,
            totalBytes,
            downloadedBytes,
            fileNames,
            currentDownloadingFile
        )
    }

    private fun publishFinished(
        numberOfFiles: Int,
        totalBytes: Long,
        fileNames: List<String>
    ) {
        _progress.value =
            DownloadOperationState.Initialized.Downloaded(numberOfFiles, totalBytes, fileNames)
    }

    private val _progress =
        MutableStateFlow<DownloadOperationState>(DownloadOperationState.Initializing)

    override val progress: StateFlow<DownloadOperationState>
        get() = _progress.asStateFlow()
}