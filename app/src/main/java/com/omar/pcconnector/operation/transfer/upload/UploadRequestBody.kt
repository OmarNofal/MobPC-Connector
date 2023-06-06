package com.omar.pcconnector.operation.transfer.upload

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink


private const val BUFFER_SIZE = 4096

class UploadRequestBody(
    private val fileUri: Uri,
    private val contentResolver: ContentResolver,
    private val progressFlow: MutableStateFlow<Float>
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
                    emitProgress(0.0f)
                    val totalSize = fileStream.available()
                    var totalSent = 0L
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (totalSent < totalSize) {
                        val read = fileStream.read(buffer, 0, BUFFER_SIZE)
                        sink.write(buffer, 0, read)
                        totalSent += read
                        emitProgress(totalSent / totalSize.toFloat())
                    }
                    emitProgress(1.0f)

            }
    }

    private fun emitProgress(percentage: Float) {
        progressFlow.value = percentage
    }
}