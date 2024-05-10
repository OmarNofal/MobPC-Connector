package com.omar.pcconnector.network.detection



// a class which implements this interface is able to
// find running servers using a particular way
interface DetectionStrategy {
    suspend fun getAvailableHosts(): List<DetectedHost>
}

interface DeviceFinder {
    suspend fun findDevice(uuid: String): DetectedHost?
}