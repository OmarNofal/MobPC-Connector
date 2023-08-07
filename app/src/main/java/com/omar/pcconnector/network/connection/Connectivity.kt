package com.omar.pcconnector.network.connection

import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.network.detection.DetectedHost
import com.omar.pcconnector.network.detection.DetectionLocalNetworkStrategy


object Connectivity {

    /**
     * Find all available servers on the phone's local network
     */
    suspend fun getDetectedServersOnLocalNetwork(): List<DetectedDevice> =
        DetectionLocalNetworkStrategy.getAvailableHosts().map { it.toDetectedDevice() }

    /**
     * Find a device with the given id on local networks on global network using Firebase
     */
    suspend fun findDevice(uuid: String, preference: ConnectionPreference = ConnectionPreference.LOCAL_NETWORK) =
        DetectionLocalNetworkStrategy.findDevice(uuid)?.toDetectedDevice()


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