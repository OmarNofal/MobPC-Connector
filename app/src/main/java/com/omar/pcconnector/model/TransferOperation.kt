package com.omar.pcconnector.model

import kotlinx.coroutines.flow.StateFlow


data class TransferOperation(
    val id: String,
    val resourceName: String,
    val transferType: TransferType,
    val transferState: TransferState
)

enum class TransferType {
    UPLOAD, DOWNLOAD
}

sealed class TransferState {

    object Initializing : TransferState()

    data class Running(
        val progress: StateFlow<TransferProgress>
    ): TransferState()

    data class Failed(
        val error: TransferError
    ): TransferState()

    object Finished: TransferState()

    object Cancelled: TransferState()
}

enum class TransferError {
    NETWORK_ERROR, FILE_SYSTEM_ERROR, UNKNOWN_ERROR
}

data class TransferProgress(val currentTransferredFile: String?, val totalBytes: Long, val transferredBytes: Long) {
    companion object {
        val default = TransferProgress("", 0, 0)
    }
}