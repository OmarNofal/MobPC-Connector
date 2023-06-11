package com.omar.pcconnector.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class WorkerEntity(
    @PrimaryKey val workerId: String,
    val workerType: WorkerType,
    val workerStatus: WorkerStatus
)



enum class WorkerType {
    UPLOAD, DOWNLOAD
}

enum class WorkerStatus {
    FINISHED, FAILED, STARTING, RUNNING, ENQUEUED
}