package com.omar.pcconnector.worker

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.omar.pcconnector.R
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.db.WorkerDao
import com.omar.pcconnector.db.WorkerException
import com.omar.pcconnector.db.WorkerType
import com.omar.pcconnector.fileSystemApi
import com.omar.pcconnector.network.connection.Connectivity
import com.omar.pcconnector.operation.transfer.upload.UploadOperation
import com.omar.pcconnector.operation.transfer.upload.UploadOperationState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Paths
import kotlin.random.Random


/**
 * Uploading files and folders
 */
@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    workerDao: WorkerDao,
    private val devicesRepository: DevicesRepository
): TransferWorker(appContext, params, workerDao) {


    private var state: UploadOperationState = UploadOperationState.Initializing

    override val workerType: WorkerType
        get() = WorkerType.UPLOAD
    override val notificationChannelName: String
        get() = "Upload"
    override val notificationChannelId: String
        get() = "upload_channel_id"

    override val notificationId: Int = Random.nextInt()
    override val notificationIcon: Int
        get() = R.drawable.baseline_cloud_upload_24

    override fun isInitializing(): Boolean = state is UploadOperationState.Initializing

    override fun notificationTitle(): String {
        return when (state) {
            is UploadOperationState.Initializing -> "Preparing to Upload"
            is UploadOperationState.Uploading -> {
                "Uploading ${(state as? UploadOperationState.Uploading)?.currentlyUploadingFile}"
            }
        }
    }

    override fun totalSize(): Long {
        return when (state) {
            is UploadOperationState.Initializing -> 1
            is UploadOperationState.Uploading -> {
                (state as? UploadOperationState.Uploading)?.totalSize ?: 0
            }
        }
    }

    override fun transferredSize(): Long {
        return when (state) {
            is UploadOperationState.Initializing -> 1
            is UploadOperationState.Uploading -> {
                (state as? UploadOperationState.Uploading)?.uploadedSize ?: 0
            }
        }
    }

    override suspend fun doWork(): Result {

        Log.i(TAG, "Worker started")

        val directoryFlags = inputData.getBooleanArray("directory_flags") ?:
            return setToFailureAndReturn(WorkerException.UNKNOWN_EXCEPTION)

        val documentFiles = inputData.getStringArray("uris")?.toDocumentFiles(directoryFlags.toTypedArray()) ?:
            return setToFailureAndReturn(WorkerException.UNKNOWN_EXCEPTION)

        val uploadPath = inputData.getString("path") ?:
            return setToFailureAndReturn(WorkerException.UNKNOWN_EXCEPTION)

        val deviceId = inputData.getString("device_id") ?: return Result.failure()


        val deviceEntity = devicesRepository.getPairedDevice(deviceId)
        val device = Connectivity.findDevice(deviceId) ?: return Result.retry()

        val connection = device.toConnection(deviceEntity.token)

        val api = connection.retrofit.fileSystemApi()

        val operation = UploadOperation(
            api,
            documentFiles,
            applicationContext.contentResolver,
            Paths.get(uploadPath)
        )

        val scope = CoroutineScope(Dispatchers.Main)
        val collectionJob = scope.launch {
            operation.progress.collect { state ->
                this@UploadWorker.state = state
                when (state) {
                    is UploadOperationState.Initializing -> setToLoading()
                    is UploadOperationState.Uploading -> updateProgress(state)
                }
                try {
                    setForeground(getForegroundInfo())
                } catch (e: Exception) { return@collect }
            }
        }

        try {
            Log.i(TAG, "Starting upload")
            operation.start()
            setToFinished()
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                setToCancelled()
            }
        }
        catch (e: IOException) {
            Log.e(TAG, e.stackTraceToString())
            Log.e(TAG, "Upload failed ${e.message}")
            return setToFailureAndReturn(WorkerException.IO_EXCEPTION)
        }
        catch (e: FileNotFoundException) {
            Log.e(TAG, "Upload failed ${e.message}")
            return setToFailureAndReturn(WorkerException.CREATE_FILE_EXCEPTION)
        }
        catch (e: Exception) {
            Log.e(TAG, "Upload failed ${e.message}")
            return setToFailureAndReturn(WorkerException.UNKNOWN_EXCEPTION)
        } finally {
            collectionJob.cancel()
        }

        return Result.success()
    }


    private fun updateProgress(state: UploadOperationState.Uploading) {
        setToRunningInDatabase()
        setProgressAsync(
            workDataOf(
                "state" to "uploading",
                "totalSize" to state.totalSize,
                "totalTransferred" to state.uploadedSize,
                "currentFile" to state.currentlyUploadingFile
            )
        )
    }

    private fun Array<String>.toDocumentFiles(directoryFlags: Array<Boolean>) = zip(directoryFlags) { uri, flag ->
        if (flag) return@zip DocumentFile.fromTreeUri(applicationContext, uri.toUri()) !!
        else return@zip DocumentFile.fromSingleUri(applicationContext, uri.toUri()) !!
    }

    companion object {
        const val TAG = "Upload Worker"
    }
}