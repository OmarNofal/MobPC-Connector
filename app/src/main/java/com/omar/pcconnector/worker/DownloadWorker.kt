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
import com.omar.pcconnector.bytesToSizeString
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.operation.transfer.download.DownloadOperation
import com.omar.pcconnector.operation.transfer.download.DownloadOperationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.file.Paths


class DownloadWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {

    init {
        createNotificationChannel()
    }

    private var state: DownloadOperationState = DownloadOperationState.Initializing

    override suspend fun doWork(): Result {

        val pathOnServer = inputData.getString("pathOnServer") ?: return Result.failure()
        val downloadPathUri = inputData.getString("downloadUri")?.toUri() ?: return Result.failure()
        val apiEndpoint = inputData.getString("api_endpoint") ?: return Result.failure()

        val api = Retrofit.Builder()
            .baseUrl(apiEndpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val downloadOperation = DownloadOperation(
            api.create(FileSystemOperations::class.java),
            Paths.get(pathOnServer),
            DocumentFile.fromTreeUri(applicationContext, downloadPathUri)!!,
            applicationContext.contentResolver
        )

        val scope = CoroutineScope(Dispatchers.Main)
        val collectionJob = scope.launch {
            downloadOperation.progress.collect {
                state = it
                when (it) {
                    is DownloadOperationState.Initializing -> {}
                    is DownloadOperationState.Cancelled -> {}
                    is DownloadOperationState.Initialized.Downloading -> {
                        updateProgressDownloading(it)
                        setForegroundAsync(getForegroundInfo())
                    }
                    else -> {
                        onFinished()
                    }
                }


            }
        }

        try {
            downloadOperation.start()
        } catch (e: Exception) {
            collectionJob.cancel()
            Log.e(TAG, e.message.toString())
            return Result.failure()
        }

        return Result.success()
    }


    private fun updateProgressDownloading(state: DownloadOperationState.Initialized.Downloading) {
        setProgressAsync(
            workDataOf(
                "state" to "downloading",
                "totalSize" to state.totalBytes,
                "totalDownloaded" to state.downloadedBytes,
                "numOfFiles" to state.numberOfFiles,
                "currentFile" to state.currentDownloadingFile
            )
        )
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val cancelAction = Notification.Action.Builder(null, "Cancel", WorkManager.getInstance(applicationContext).createCancelPendingIntent(id))
            .build()

        val downloadingFile = (state as? DownloadOperationState.Initialized.Downloading)?.currentDownloadingFile
        val totalSize = (state as? DownloadOperationState.Initialized.Downloading)?.totalBytes?.toInt() ?: 1
        val totalDownloaded = (state as? DownloadOperationState.Initialized.Downloading)?.downloadedBytes?.toInt() ?: 0

        val title = if (downloadingFile == null) "Download in Progress" else "Downloading $downloadingFile"

        val notification = Notification.Builder(applicationContext, "download_channel_id")
            .setContentTitle(title)
            .setStyle(Notification.BigTextStyle().bigText("${totalDownloaded.toLong().bytesToSizeString()} / ${totalSize.toLong().bytesToSizeString()}"))
            .setContentText("${totalDownloaded.toLong().bytesToSizeString()} / ${totalSize.toLong().bytesToSizeString()}")
            .setProgress(totalSize, totalDownloaded, state is DownloadOperationState.Initializing)
            .setSmallIcon(Icon.createWithResource(applicationContext, R.drawable.baseline_cloud_download_24))
            .addAction(cancelAction)
            .setOnlyAlertOnce(true)
            .build()


        return ForegroundInfo(13, notification)
    }

    private fun onFinished() {

    }

    private fun createNotificationChannel(): NotificationChannel {
        val notificationChannel = NotificationChannel(
            "download_channel_id",
            "Download",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java) as NotificationManager
        return notificationChannel.also { notificationManager.createNotificationChannel(it) }
    }

    companion object {
        private const val TAG = "Download Worker"
    }

}