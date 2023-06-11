package com.omar.pcconnector.model



data class TransferOperation(
    val id: String,
    val transferType: TransferType,
    val transferState: TransferState
)

enum class TransferType {
    UPLOAD, DOWNLOAD
}

sealed class TransferState() {

    object Initializing : TransferState()

    data class Running(
        val filesName: List<String>,
        val currentTransferredFile: String?,
        val totalBytes: Long,
        val transferredBytes: Long
    ): TransferState()

    data class Failed(
        val filesName: List<String>,
        val throwable: Throwable
    ): TransferState()

    data class Finished(
        val numberOfFiles: Int
    ): TransferState()
}