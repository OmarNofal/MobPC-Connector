package com.omar.pcconnector.operation.transfer.download

import android.content.ContentResolver
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.JsonParser
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.fillBuffer
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.exceptions.InvalidResponseException
import com.omar.pcconnector.operation.CreateAFileException
import com.omar.pcconnector.operation.MonitoredOperation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Long.min
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.math.pow


private val BUFFER_SIZE = (2.0).pow(22).toInt()

sealed class DownloadOperationState {

    object Initializing : DownloadOperationState()

    class Downloading(
        val totalBytes: Long,
        val downloadedBytes: Long,
        val currentDownloadingFile: String
    ) : DownloadOperationState()

}


class DownloadOperation(
    private val api: FileSystemOperations,
    private val pathOnServer: Path,
    private val downloadPath: DocumentFile,
    private val contentResolver: ContentResolver
) : MonitoredOperation<DownloadOperationState, List<Uri>>() {


    override val name: String
        get() = "Download Operation"
    override val operationDescription: String
        get() = "Downloading file(s)"


    private lateinit var response: Response<ResponseBody>

    override suspend fun start(): List<Uri> {

        if (!downloadPath.isDirectory) {
            throw IllegalArgumentException("Destination Path is not a directory")
        }

        response = api.download(pathOnServer.absolutePath)

        val contentDisposition = response.headers().get("Content-Disposition")
        val contentType = response.headers().get("Content-Type")

        // If there is content disposition in the header then we are downloading a file
        // else we are downloading a folder
        return if (contentDisposition != null) handleDownloadFile()
        else
            when (contentType) {
                "folder" -> handleDownloadFolder()
                else -> throw (UnsupportedOperationException())
            }
    }

    private suspend fun handleDownloadFile() = withContext(Dispatchers.IO) {

        val body = response.body()!!
        val fileName =
            response
                .headers()
                .get("Content-Disposition")!!
                .substringAfter("filename=")
                .removeSuffix("\"")
                .removePrefix("\"")

        val fileSize = response.headers().get("Content-Length")!!.toLong()

        publishProgress(fileSize, 0L, fileName)

        val deviceFile = downloadPath.createFile(
            "no_mime_type/no", // invalid mimeType to prevent android from adding its extension to the file
            fileName
        ) ?: throw CreateAFileException(fileName)

        contentResolver.openOutputStream(deviceFile.uri)?.buffered(BUFFER_SIZE)
            .use { fileStream ->
                val source = body.byteStream()

                try {
                    source.use { it ->
                        val buffer = ByteArray(1024 * 256)
                        var totalRead: Long = 0
                        var read: Int
                        while (it.read(buffer, 0, 1024 * 256)
                                .also { read = it } != -1 && isActive
                        ) {
                            totalRead += read
                            fileStream!!.write(buffer, 0, read)
                            publishProgress(fileSize, totalRead, fileName)
                        }
                        if (!isActive) {
                            throw CancellationException("Download was cancelled")
                        }
                    }
                } catch (e: Exception) { // unroll
                    deviceFile.delete()
                    throw e
                }
            }
        return@withContext listOf(deviceFile.uri)
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

        val savedFiles: MutableList<DocumentFile> =
            mutableListOf() // used to delete created files in case of exception

        try {
            //val numberOfFiles = json["numberOfFiles"].asInt
            val totalSize = json["totalSize"].asLong
            val files = json["files"].asJsonArray
            var totalRead = 0L

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
                    else return@fold acc.createDirectory(folderName)
                        ?: throw CreateAFileException("Failed to create directory")
                }

                val deviceFile = fileDir.createFile("no_mime_type/no", fileName)
                    ?: throw CreateAFileException("Failed to create $fileName")
                savedFiles.add(deviceFile)

                contentResolver.openOutputStream(deviceFile.uri)!!
                    .buffered(BUFFER_SIZE)
                    .use { fileStream ->
                        var read: Int
                        var fileTotalRead = 0
                        val buffer = ByteArray(BUFFER_SIZE)

                        while (fileTotalRead < fileSize && isActive) {
                            read = body
                                .inputStream()
                                .read(
                                    buffer,
                                    0,
                                    min(BUFFER_SIZE.toLong(), fileSize - fileTotalRead).toInt()
                                )
                            fileTotalRead += read
                            totalRead += read
                            fileStream.write(buffer, 0, read)
                            publishProgress(totalSize, totalRead, fileName)
                        }

                        ensureActive()
                    }
            }
            return@withContext savedFiles.map { it.uri }
        } catch (n: NullPointerException) {
            throw InvalidResponseException("Invalid message from the server")
        } catch (e: Exception) {
            savedFiles.forEach { it.delete() }
            throw e
        } finally {
            body.close()
        }
    }


    private fun publishProgress(
        totalBytes: Long,
        downloadedBytes: Long,
        currentDownloadingFile: String
    ) {
        _progress.value = DownloadOperationState.Downloading(
            totalBytes,
            downloadedBytes,
            currentDownloadingFile
        )
    }

    private val _progress =
        MutableStateFlow<DownloadOperationState>(DownloadOperationState.Initializing)

    override val progress: StateFlow<DownloadOperationState>
        get() = _progress.asStateFlow()
}
