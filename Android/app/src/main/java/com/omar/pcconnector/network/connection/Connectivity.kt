package com.omar.pcconnector.network.connection

import android.util.Log
import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.network.detection.DetectedHost
import com.omar.pcconnector.network.detection.DetectionLocalNetworkStrategy
import com.omar.pcconnector.network.detection.FirebaseDeviceFinder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope


object Connectivity {

    /**
     * Find all available servers on the phone's local network
     */
    suspend fun getDetectedServersOnLocalNetwork(): List<DetectedDevice> =
        DetectionLocalNetworkStrategy.getAvailableHosts()
            .map { it.toDetectedDevice() }

    /**
     * Find a device with the given id on local networks on global network using Firebase
     */
    suspend fun findDevice(
        uuid: String,
        preference: ConnectionPreference = ConnectionPreference.ANY
    ): DetectedDevice? {
        return when (preference) {
            ConnectionPreference.WIDE_NETWORK -> FirebaseDeviceFinder.findDevice(
                uuid
            )?.toDetectedDevice()

            ConnectionPreference.LOCAL_NETWORK -> DetectionLocalNetworkStrategy.findDevice(
                uuid
            )?.toDetectedDevice()

            ConnectionPreference.ANY -> {
                val device = supervisorScope {
                    val localDeviceTask = async {
                        DetectionLocalNetworkStrategy.findDevice(uuid)?.toDetectedDevice()
                    }
                    val globalDeviceTask = async {
                        FirebaseDeviceFinder.findDevice(uuid)?.toDetectedDevice()
                    }
                    try {
                        val localResult = localDeviceTask.await()
                        localResult?.let {
                            Log.d("LOCAL RESULT", it.toString())
                            globalDeviceTask.cancel()
                            return@supervisorScope it
                        }
                        val globalResult = globalDeviceTask.await()
                        globalResult?.let {
                            Log.d("GLOBAL RESULT", it.toString())
                            return@supervisorScope it
                        }
                        null
                    } catch (e: Exception) {
                        Log.e("CONNECTIVITY", e.stackTraceToString())
                        null
                    }
                }
                device
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