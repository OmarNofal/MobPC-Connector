package com.omar.pcconnector.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omar.pcconnector.model.TransferError


@Entity
data class WorkerEntity(
    @PrimaryKey val workerId: String,
    val workerType: WorkerType,
    val workerStatus: WorkerStatus,
    val resourceName: String,
    val exception: WorkerException? = null
)


enum class WorkerType {
    UPLOAD, DOWNLOAD
}

enum class WorkerStatus {
    FINISHED, FAILED, STARTING, RUNNING, ENQUEUED, CANCELLED
}

enum class WorkerException {
    IO_EXCEPTION, CREATE_FILE_EXCEPTION, UNKNOWN_EXCEPTION;

    fun toDomainTransferError() = when(this) {
        IO_EXCEPTION -> TransferError.NETWORK_ERROR
        CREATE_FILE_EXCEPTION -> TransferError.FILE_SYSTEM_ERROR
        UNKNOWN_EXCEPTION -> TransferError.UNKNOWN_ERROR
    }
}