package com.omar.pcconnector.operation.transfer


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.omar.pcconnector.db.WorkerDao
import com.omar.pcconnector.db.WorkerEntity
import com.omar.pcconnector.db.WorkerStatus
import com.omar.pcconnector.db.WorkerType
import com.omar.pcconnector.model.TransferError
import com.omar.pcconnector.model.TransferOperation
import com.omar.pcconnector.model.TransferProgress
import com.omar.pcconnector.model.TransferState
import com.omar.pcconnector.model.TransferType
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.operation.transfer.download.DownloadOperation
import com.omar.pcconnector.worker.DownloadWorker
import com.omar.pcconnector.worker.UploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.absolutePathString
import kotlin.io.path.name


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
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            deleteUnavailableWorkers()
        }
    }

    val currentTransfers: Flow<List<TransferOperation>>
            by lazy {
                workerDao.getAllWorkersFlow()
                    .map { workers ->
                        workers.map { workerEntity ->
                            val id = workerEntity.workerId
                            val resourceName = workerEntity.resourceName
                            val transferType =
                                if (workerEntity.workerType == WorkerType.DOWNLOAD) TransferType.DOWNLOAD else TransferType.UPLOAD

                            val state: TransferState = when (workerEntity.workerStatus) {
                                WorkerStatus.FINISHED -> TransferState.Finished
                                WorkerStatus.FAILED -> TransferState.Failed(
                                    workerEntity.exception?.toDomainTransferError()
                                        ?: TransferError.UNKNOWN_ERROR
                                )

                                WorkerStatus.STARTING -> TransferState.Initializing
                                WorkerStatus.ENQUEUED -> TransferState.Initializing
                                WorkerStatus.RUNNING -> getTransferProgressFromWorker(id)
                                WorkerStatus.CANCELLED -> TransferState.Cancelled
                            }

                            TransferOperation(id, resourceName, transferType, state)
                        }
                    }
            }


    fun download(
        deviceId: String,
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
                        "downloadUri" to downloadPath.uri.toString(),
                        "device_id" to deviceId
                    )
                ).build()

        workManager.enqueue(downloadWorkerRequest)
        scope.launch {
            workerDao.insertWorker(
                WorkerEntity(
                    workerId.toString(),
                    deviceId,
                    WorkerType.DOWNLOAD,
                    WorkerStatus.ENQUEUED,
                    pathOnServer.name
                )
            )
        }
    }


    fun upload(
        files: List<DocumentFile>,
        deviceId: String,
        path: Path
    ) {
        if (files.isEmpty()) return

        val workerId = UUID.randomUUID()

        val uploadWorkerRequest =
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(workerId)
                .setInputData(
                    workDataOf(
                        "uris" to files.map { it.uri.toString() }.toTypedArray(),
                        "path" to path.absolutePathString(),
                        "directory_flags" to files.toDirectoryFlags().toTypedArray(),
                        "device_id" to deviceId
                    )
                ).build()

        val resourceName = if (files.size == 1) files[0].name ?: "Unknown"
        else "${files[0]} and ${files.size - 1} others"
        scope.launch {
            workerDao.insertWorker(
                WorkerEntity(
                    workerId.toString(),
                    deviceId,
                    WorkerType.UPLOAD,
                    WorkerStatus.ENQUEUED,
                    resourceName
                )
            )
        }

        workManager.enqueue(uploadWorkerRequest)
    }

    private suspend fun deleteUnavailableWorkers() {
        workerDao.deleteNonRunningWorkers()
        workerDao.getAllIds()
            .forEach { id ->
                withContext(Dispatchers.Main) {
                    val observerLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(id))
                    observerLiveData.observeForever(object : Observer<WorkInfo> {
                        override fun onChanged(value: WorkInfo) {
                            observerLiveData.removeObserver(this)
                            if (value.state in arrayOf(
                                    WorkInfo.State.CANCELLED,
                                    WorkInfo.State.SUCCEEDED,
                                    WorkInfo.State.FAILED
                                )
                            ) {
                                scope.launch {
                                    workerDao.deleteWork(id)
                                }
                            }
                        }
                    }
                    )
                }
            }
    }

    private fun getTransferProgressFromWorker(id: String): TransferState {
        val state = MutableStateFlow(TransferProgress.default)
        workManager.getWorkInfoByIdLiveData(UUID.fromString(id)).apply {
            observeForever {
                val progress = it.progress
                val totalSize = progress.getLong("totalSize", 0)
                val totalTransferred = progress.getLong("totalTransferred", 0)
                val currentFile = progress.getString("currentFile")

                state.value = TransferProgress(currentFile, totalSize, totalTransferred)
            }
        }
        return TransferState.Running(state)
    }

    fun cancelWorker(id: String) {
        Log.i("MANAGER", "Cancelling worker $id")
        workManager.cancelWorkById(UUID.fromString(id))
    }

    fun deleteWorker(id: String) {
        scope.launch {
            workerDao.deleteWork(id)
        }
    }

    /**
     * Download temporary file suitable for downloading a file
     * quickly for sharing. This is a blocking call and will
     * return the download file handle when finished or throw an
     * exception
     *
     * @return A uri of the downloaded file
     * @throws FileNotFoundException if the file could not be downloaded
     */
    suspend fun downloadTemporaryFile(
        api: FileSystemOperations,
        pathOnServer: Path
    ): Uri {
        val tempFolder = context.externalCacheDir!!
        val downloadOperation = DownloadOperation(
            api,
            pathOnServer,
            DocumentFile.fromFile(tempFolder),
            context.contentResolver
        )
        return downloadOperation.start().firstOrNull() ?: throw FileNotFoundException()
    }

    fun copyFile(source: Uri, destination: DocumentFile) {
        context.contentResolver.openInputStream(source)
            .use { inStream ->
                context.contentResolver.openOutputStream(destination.uri)
                    .use { outStream ->
                        inStream?.copyTo(outStream!!) ?: return
                    }
            }
    }

    private fun List<DocumentFile>.toDirectoryFlags(): List<Boolean> = map { it.isDirectory }
}