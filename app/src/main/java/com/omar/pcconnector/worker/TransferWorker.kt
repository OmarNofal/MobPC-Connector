package com.omar.pcconnector.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.omar.pcconnector.db.WorkerDao
import com.omar.pcconnector.db.WorkerEntity
import com.omar.pcconnector.db.WorkerException
import com.omar.pcconnector.db.WorkerStatus
import com.omar.pcconnector.db.WorkerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


abstract class TransferWorker(
    appContext: Context,
    params: WorkerParameters,
    private val workerDao: WorkerDao
): CoroutineWorker(appContext, params) {

    private val databaseScope = CoroutineScope(Dispatchers.IO)

    private val resourceName =
        runBlocking {
            workerDao.getById(this@TransferWorker.id.toString()).resourceName
        }

    abstract val workerType: WorkerType

    abstract val notificationChannelName: String
    abstract val notificationChannelId: String
    abstract val notificationId: Int
    abstract val notificationIcon: Int

    abstract fun isInitializing(): Boolean
    abstract fun notificationTitle(): String

    abstract fun totalSize(): Long
    abstract fun transferredSize(): Long

    init {
        createNotificationChannel()
    }

    inline fun <reified T> getApi(): T? {
        val apiEndpoint = inputData.getString("api_endpoint") ?: return null

        return Retrofit.Builder()
            .baseUrl(apiEndpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(T::class.java)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val cancelAction = Notification.Action.Builder(null, "Cancel", WorkManager.getInstance(applicationContext).createCancelPendingIntent(id))
            .build()

        val notification = Notification.Builder(applicationContext, notificationChannelId)
            .setContentTitle(notificationTitle())
            .setProgress(totalSize().toInt(), transferredSize().toInt(), isInitializing())
            .setSmallIcon(Icon.createWithResource(applicationContext, notificationIcon))
            .addAction(cancelAction)
            .setOnlyAlertOnce(true)
            .build()

        return ForegroundInfo(notificationId, notification)
    }


    private var isSetToRunningInDatabase = false
    fun setToRunningInDatabase() {
        if (!isSetToRunningInDatabase) {
            databaseScope.launch { workerDao.updateWorker(WorkerEntity(id.toString(), workerType, WorkerStatus.RUNNING, resourceName)) }
            isSetToRunningInDatabase = true
        }
    }

    suspend fun setToLoading() {
        databaseScope.launch { workerDao.updateWorker(WorkerEntity(id.toString(), workerType, WorkerStatus.STARTING, resourceName)) }
        setProgress(
            workDataOf(
                "state" to "loading"
            )
        )
    }

    suspend fun setToFailureAndReturn(cause: WorkerException): Result {
        workerDao.updateWorker(WorkerEntity(id.toString(), workerType, WorkerStatus.FAILED, resourceName, cause))
        return Result.failure()
    }

    suspend fun setToFinished() {
        workerDao.updateWorker(WorkerEntity(id.toString(), workerType, WorkerStatus.FINISHED, resourceName))
        setProgress(
            workDataOf(
                "state" to "finished"
            )
        )
    }


    suspend fun setToCancelled() {
        workerDao.updateWorker(WorkerEntity(id.toString(), workerType, WorkerStatus.CANCELLED, resourceName))
        setProgressAsync(
            workDataOf(
                "state" to "cancelled"
            )
        )
    }


    private fun createNotificationChannel(): NotificationChannel {
        val notificationChannel = NotificationChannel(
            notificationChannelId,
            notificationChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java) as NotificationManager
        return notificationChannel.also { notificationManager.createNotificationChannel(it) }
    }

}