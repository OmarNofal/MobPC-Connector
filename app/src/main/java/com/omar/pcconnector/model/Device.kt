package com.omar.pcconnector.model


/**
 * Info of some server
 */
data class DeviceInfo(
    val id: String,
    val name: String,
    val os: String
)

/**
 * A server which the user connected to before and contains the token
 */
data class PairedDevice(
    val deviceInfo: DeviceInfo,
    val token: String,
    val autoConnect: Boolean
)

/**
 * A device which is detected but not yet connected to
 */
data class DetectedDevice(
    val deviceInfo: DeviceInfo,
    val ip: String,
    val port: String
)