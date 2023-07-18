package com.omar.pcconnector.worker

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.omar.pcconnector.R
import com.omar.pcconnector.db.WorkerDao
import com.omar.pcconnector.db.WorkerException
import com.omar.pcconnector.db.WorkerType
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.operation.transfer.download.DownloadOperation
import com.omar.pcconnector.operation.transfer.download.DownloadOperationState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Paths
import kotlin.random.Random


@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context, @Assisted params: WorkerParameters,
    workerDao: WorkerDao
): TransferWorker(appContext, params, workerDao) {

    private var state: DownloadOperationState = DownloadOperationState.Initializing
    override val workerType: WorkerType
        get() = WorkerType.DOWNLOAD

    override val notificationChannelName: String
        get() = "Download"

    override val notificationChannelId: String
        get() = "download_channel_id"

    override val notificationId: Int = Random.nextInt()

    override val notificationIcon: Int = R.drawable.baseline_cloud_download_24

    override fun isInitializing(): Boolean = state is DownloadOperationState.Initializing

    override fun notificationTitle(): String {
        return when (state) {
            is DownloadOperationState.Initializing -> "Preparing to Download"
            is DownloadOperationState.Downloading -> "Downloading ${(state as? DownloadOperationState.Downloading)?.currentDownloadingFile}"
        }
    }

    override fun totalSize(): Long {
        return when (state) {
            is DownloadOperationState.Initializing -> 1L
            is DownloadOperationState.Downloading -> (state as? DownloadOperationState.Downloading)?.totalBytes ?: 0
        }
    }

    override fun transferredSize(): Long {
        return when (state) {
            is DownloadOperationState.Initializing -> 1L
            is DownloadOperationState.Downloading -> (state as? DownloadOperationState.Downloading)?.downloadedBytes ?: 0
        }
    }


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


        val collectionJob = CoroutineScope(Dispatchers.Main).launch {
            downloadOperation.progress.collect {
                state = it
                when (it) {
                    is DownloadOperationState.Initializing -> { setToLoading() }
                    is DownloadOperationState.Downloading -> { updateProgressDownloading(it) }
                }
                try {
                    setForeground(getForegroundInfo())
                } catch (e: Exception) { return@collect }
            }
        }


        try {
            downloadOperation.start()
            setToFinished()
        }
        catch(e: CancellationException) {
            Log.e(TAG, "Download was cancelled")
            withContext(NonCancellable) {
                setToCancelled()
            }
        }
        catch (e: IOException) {
            return setToFailureAndReturn(WorkerException.IO_EXCEPTION)
        }
        catch (e: FileNotFoundException) {
            return setToFailureAndReturn(WorkerException.CREATE_FILE_EXCEPTION)
        }
        catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
            return setToFailureAndReturn(WorkerException.UNKNOWN_EXCEPTION)
        }
        finally {
            collectionJob.cancel()
        }


        return Result.success()
    }


    private suspend fun updateProgressDownloading(state: DownloadOperationState.Downloading) {
        setToRunningInDatabase()
        setProgress(
            workDataOf(
                "state" to "downloading",
                "totalSize" to state.totalBytes,
                "totalTransferred" to state.downloadedBytes,
                "currentFile" to state.currentDownloadingFile
            )
        )
    }


    companion object {
        private const val TAG = "Download Worker"
    }

}