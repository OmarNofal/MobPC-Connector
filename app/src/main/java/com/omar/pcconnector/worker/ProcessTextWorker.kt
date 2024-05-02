package com.omar.pcconnector.worker

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.network.connection.Connectivity
import com.omar.pcconnector.operation.CopyToClipboardOperation
import com.omar.pcconnector.operation.OpenLinkOperation
import com.omar.pcconnector.pcApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ProcessTextWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val devicesRepository: DevicesRepository,
): CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        val action = inputData.getString(ACTION_KEY)
        val data = inputData.getString(DATA_KEY)
        val deviceId = inputData.getString(DEVICE_KEY)

        if (
            action !in listOf(ACTION_BROWSER, ACTION_COPY)
            ||
            data == null
            ||
            deviceId == null
            ) {
            Log.e("PROCESS WORKER", "Invalid Params bruv")
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Invalid arguments", Toast.LENGTH_SHORT).show()
            }
            return Result.failure()
        }

        try {
            val connection = getConnection(deviceId)
            val api = connection.retrofit.pcApi()
            when (action) {
                ACTION_BROWSER -> {
                    OpenLinkOperation(api, data, false).start()
                }
                ACTION_COPY -> {
                    CopyToClipboardOperation(api, data).start()
                }
            }
        } catch (e: Exception) {
            Log.e("PROCESS WORKER", e.stackTraceToString())
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Device offline", Toast.LENGTH_SHORT).show()
            }
            return Result.failure()
        }

        Log.i("PROCESS WORKER", "SUCCESS")
        val successText = when (action) {
            ACTION_COPY -> "Copied Successfully"
            ACTION_BROWSER -> "Opened in Browser Successfully"
            else -> "Success"
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, successText, Toast.LENGTH_SHORT).show()
        }
        return Result.success()
    }


    private suspend fun getConnection(deviceId: String): Connection {
        val device = devicesRepository.getPairedDevice(deviceId)
        val connection = Connectivity.findDevice(deviceId)

        return connection!!.toConnection(device.token)
    }


    companion object {
        const val ACTION_COPY = "COPY"
        const val ACTION_BROWSER = "BROWSER"

        const val ACTION_KEY = "ACTION"
        const val DATA_KEY = "DATA"
        const val DEVICE_KEY = "DEVICE"
    }
}