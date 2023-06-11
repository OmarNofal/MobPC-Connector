package com.omar.pcconnector.operation.transfer


import android.content.Context
import androidx.documentfile.provider.DocumentFile
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.omar.pcconnector.db.WorkerDao
import com.omar.pcconnector.db.WorkerEntity
import com.omar.pcconnector.db.WorkerStatus
import com.omar.pcconnector.db.WorkerType
import com.omar.pcconnector.model.TransferOperation
import com.omar.pcconnector.model.TransferState
import com.omar.pcconnector.model.TransferType
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.toDownloadTransferState
import com.omar.pcconnector.toUploadTransferState
import com.omar.pcconnector.worker.DownloadWorker
import com.omar.pcconnector.worker.UploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.nio.file.Path
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.absolutePathString


/**
 * This is a singleton class which is a single source of truth for all
 * download and upload operations. It is responsible for starting, canceling and monitoring all transfers
 */
@Singleton
class TransfersManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workerDao: WorkerDao
) {

    private val workManager = WorkManager.getInstance(context)

    private val currentTransfers: Flow<List<Flow<TransferOperation>>>
        by lazy {
            workerDao.getActiveWorkersFlow()
                .map { workers ->
                    workers.map { workerEntity ->
                        val id = workerEntity.workerId
                        val transferType =
                            if (workerEntity.workerType == WorkerType.DOWNLOAD) TransferType.DOWNLOAD else TransferType.UPLOAD
                        val state = MutableStateFlow(
                            TransferOperation(
                                id,
                                transferType,
                                TransferState.Initializing
                            )
                        )
                        workManager.getWorkInfoByIdLiveData(UUID.fromString(id))
                            .observeForever {
                                val transferState = if (transferType == TransferType.UPLOAD) it.toUploadTransferState()
                                else it.toDownloadTransferState()

                                state.value = TransferOperation(id, transferType, transferState)
                            }
                        state
                    }
                }
        }



    fun download(
        connection: Connection,
        pathOnServer: Path,
        downloadPath: DocumentFile
    ) {

        val workerId = UUID.randomUUID()


        val downloadWorkerRequest =
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(workerId)
                .setInputData(
                    workDataOf(
                        "pathOnServer" to pathOnServer.absolutePathString(),
                        "api_endpoint" to connection.retrofit.baseUrl().toString(),
                        "downloadUri" to downloadPath.uri.toString(),
                    )
                ).build()

        workManager.enqueue(downloadWorkerRequest)
        workerDao.insertWorker(
            WorkerEntity(workerId.toString(), WorkerType.DOWNLOAD, WorkerStatus.ENQUEUED)
        )
    }


    fun upload(
        files: List<DocumentFile>,
        connection: Connection,
        path: Path
    ) {

        val workerId = UUID.randomUUID()

        val uploadWorkerRequest =
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(workerId)
                .setInputData(
                    workDataOf(
                        "uris" to files.map { it.uri.toString() }.toTypedArray(),
                        "api_endpoint" to connection.retrofit.baseUrl().toString(),
                        "path" to path.absolutePathString(),
                        "directory_flags" to files.toDirectoryFlags().toTypedArray()
                    )
                ).build()

        workManager.enqueue(uploadWorkerRequest)
        workerDao.insertWorker(
            WorkerEntity(workerId.toString(), WorkerType.UPLOAD, WorkerStatus.ENQUEUED)
        )
    }

    private fun List<DocumentFile>.toDirectoryFlags(): List<Boolean> = map { it.isDirectory }
}