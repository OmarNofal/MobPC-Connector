package com.omar.pcconnector.operation.transfer.upload

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink


private const val BUFFER_SIZE = 16384

class UploadRequestBody(
    private val fileUri: Uri,
    private val contentResolver: ContentResolver,
    private val progressFlow: MutableStateFlow<Pair<Long, Long>>
): RequestBody() {


    override fun contentType(): MediaType? {
        return null
    }

    override fun contentLength(): Long {
        return contentResolver.openFileDescriptor(fileUri, "r").use { it?.statSize ?: -1L }
    }

    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(fileUri)
            .use { fileStream ->

                    if (fileStream == null) throw Exception("Failed to open the file")
                    emitProgress(0L, 0L)
                    val totalSize = fileStream.available().toLong()
                    var totalSent = 0L
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (totalSent < totalSize) {
                        val read = fileStream.read(buffer, 0, BUFFER_SIZE)
                        sink.write(buffer, 0, read)
                        totalSent += read
                        emitProgress(totalSize, totalSent)
                    }
                    emitProgress(totalSize, totalSize)
            }
    }

    private fun emitProgress(totalSize: Long, uploadedSize: Long) {
        progressFlow.value = uploadedSize to totalSize
    }
}