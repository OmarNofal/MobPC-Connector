package com.omar.pcconnector.network.connection

import android.util.Log
import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.network.detection.DetectedHost
import com.omar.pcconnector.network.detection.DetectionLocalNetworkStrategy
import com.omar.pcconnector.network.detection.FirebaseDetectionStrategy
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


object Connectivity {

    /**
     * Find all available servers on the phone's local network
     */
    suspend fun getDetectedServersOnLocalNetwork(): List<DetectedDevice> =
        DetectionLocalNetworkStrategy.getAvailableHosts().map { it.toDetectedDevice() }

    /**
     * Find a device with the given id on local networks on global network using Firebase
     */
    suspend fun findDevice(
        uuid: String,
        preference: ConnectionPreference = ConnectionPreference.LOCAL_NETWORK
    ): DetectedDevice? {
        return when (preference) {
            ConnectionPreference.WIDE_NETWORK -> FirebaseDetectionStrategy.findDevice(uuid)?.toDetectedDevice()
            ConnectionPreference.LOCAL_NETWORK -> DetectionLocalNetworkStrategy.findDevice(uuid)?.toDetectedDevice()
            ConnectionPreference.ANY -> {
                coroutineScope {
                    val localDeviceTask =
                        async { DetectionLocalNetworkStrategy.findDevice(uuid)?.toDetectedDevice() }
                    val globalDeviceTask =
                        async { FirebaseDetectionStrategy.findDevice(uuid)?.toDetectedDevice() }
                    try {
                        localDeviceTask.await() ?: globalDeviceTask.await()
                    } catch (e: Exception) {
                        Log.e("CONNECTIVITY", e.stackTraceToString())
                        null
                    }
                }
            }
        }
    }

    private fun DetectedHost.toDetectedDevice() =
        DetectedDevice(
            DeviceInfo(uuid, serverName, os),
            ipAddress,
            port
        )
}

enum class ConnectionPreference {
    LOCAL_NETWORK, WIDE_NETWORK, ANY
}