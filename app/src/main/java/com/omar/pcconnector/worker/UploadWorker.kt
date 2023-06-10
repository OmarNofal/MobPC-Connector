package com.omar.pcconnector.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.omar.pcconnector.R
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.operation.transfer.upload.UploadOperation
import com.omar.pcconnector.operation.transfer.upload.UploadOperationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.file.Paths


/**
 * Uploading files and folders
 */
class UploadWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {

    init {
        createNotificationChannel()
    }

    private var state: UploadOperationState = UploadOperationState.Initializing(listOf())


    override suspend fun doWork(): Result {

        Log.i(TAG, "Worker started")

        val directoryFlags = inputData.getBooleanArray("directory_flags") ?: return Result.failure()
        val documentFiles = inputData.getStringArray("uris")?.toDocumentFiles(directoryFlags.toTypedArray()) ?: return Result.failure()
        val uploadPath = inputData.getString("path") ?: return Result.failure()
        val apiEndpoint = inputData.getString("api_endpoint") ?: return Result.failure()

        val api = Retrofit.Builder()
            .baseUrl(apiEndpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val operation = UploadOperation(
            api.create(FileSystemOperations::class.java),
            documentFiles,
            applicationContext.contentResolver,
            Paths.get(uploadPath)
        )

        val scope = CoroutineScope(Dispatchers.Main)
        val collectionJob = scope.launch {
            operation.progress.collect { state ->
                this@UploadWorker.state = state
                when (state) {
                    is UploadOperationState.Initializing -> {
                        updateProgressLoading()
                    }
                    is UploadOperationState.Initialized.Uploading -> {
                        updateProgress(state)
                    }
                    is UploadOperationState.Failed -> {

                    }
                }
                setForeground(getForegroundInfo())
            }
        }

        try {
            Log.i(TAG, "Starting upload")
            operation.start()
        } catch (e: Exception) {
            collectionJob.cancel()
            Log.e(TAG, "Upload failed ${e.message}")
            return Result.failure()
        }

        return Result.success()
    }


    override suspend fun getForegroundInfo(): ForegroundInfo {
        val cancelAction = Notification.Action.Builder(null, "Cancel", WorkManager.getInstance(applicationContext).createCancelPendingIntent(id))
            .build()

        val totalSize = (state as? UploadOperationState.Initialized.Uploading)?.totalSize?.toInt() ?: 0
        val uploaded = (state as? UploadOperationState.Initialized.Uploading)?.uploadedSize?.toInt() ?: 0
        val uploadingFile = (state as? UploadOperationState.Initialized.Uploading)?.currentlyUploadingFile

        val title = if (uploadingFile == null) "Upload in Progress" else "Uploading $uploadingFile"

        val notification = Notification.Builder(applicationContext, "upload_channel_id")
            .setContentTitle(title)
            .setProgress(totalSize, uploaded, state is UploadOperationState.Initializing)
            .setSmallIcon(Icon.createWithResource(applicationContext, R.drawable.baseline_cloud_upload_24))
            .addAction(cancelAction)
            .setOnlyAlertOnce(true)
            .build()


        return ForegroundInfo(12, notification)
    }

    private fun createNotificationChannel(): NotificationChannel {
        val notificationChannel = NotificationChannel(
            "upload_channel_id",
            "Upload",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java) as NotificationManager
        return notificationChannel.also { notificationManager.createNotificationChannel(it) }
    }

    private fun updateProgress(state: UploadOperationState.Initialized.Uploading) {
        setProgressAsync(
            workDataOf(
                "state" to "uploading",
                "files" to state.filesNames.toTypedArray(),
                "totalSize" to state.totalSize,
                "totalDownloaded" to state.uploadedSize,
                "currentlyUploading" to state.currentlyUploadingFile
            )
        )
    }

    private fun updateProgressLoading() {
        setProgressAsync(
            workDataOf(
                "state" to "loading"
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