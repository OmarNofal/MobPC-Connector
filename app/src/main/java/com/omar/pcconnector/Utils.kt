package com.omar.pcconnector

import androidx.work.WorkInfo
import com.omar.pcconnector.model.TransferState
import java.lang.IllegalStateException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.math.pow


val Path.absolutePath: String
    get() = absolutePathString().removePrefix("/")



val sizeRanges = arrayOf(
    2.0.pow(10.0).toLong() until 2.0.pow(20.0).toLong() to "KB",
    2.0.pow(20.0).toLong() until 2.0.pow(30.0).toLong() to "MB",
    2.0.pow(30.0).toLong() until Long.MAX_VALUE to "GB"
)

// Converts size in bytes to human-readable format
// ex 4096bytes = 4KB
fun Long.bytesToSizeString(): String {

    if (this in 0 until 1024) return "$this Bytes"

    val result = try {
        val sizeRange = sizeRanges.first { this in it.first }
        "${this / sizeRange.first.first} ${sizeRange.second}"
    } catch (e: NoSuchElementException) {
        "Unknown size"
    }

    return result
}



fun WorkInfo.toUploadTransferState(): TransferState {
    val state = outputData.getString("state") ?: "loading"
    if (state == "loading") return TransferState.Initializing
    if (state == "finished") return TransferState.Finished(outputData.getInt("numberOfFiles", 0))


    val totalSize = outputData.getLong("totalSize", -1)
    val totalUploaded = outputData.getLong("totalUploaded", -1)
    val currentlyUploading = outputData.getString("currentlyUploading")

    if ((totalSize == -1L) or (totalUploaded == -1L)) throw IllegalStateException()
    return TransferState.Running(listOf(), currentlyUploading, totalSize, totalUploaded)
}

fun WorkInfo.toDownloadTransferState(): TransferState {
    val state = outputData.getString("state") ?: "loading"
    if (state == "loading") return TransferState.Initializing
    if (state == "finished") return TransferState.Finished(outputData.getInt("numberOfFiles", 0))

    val totalSize = outputData.getLong("totalSize", -1)
    val totalDownloaded = outputData.getLong("totalDownloaded", -1)
    val currentFile = outputData.getString("currentFile")

    if ((totalSize == -1L) or (totalDownloaded == -1L)) throw IllegalStateException()
    return TransferState.Running(listOf(), currentFile, totalSize, totalDownloaded)
}
