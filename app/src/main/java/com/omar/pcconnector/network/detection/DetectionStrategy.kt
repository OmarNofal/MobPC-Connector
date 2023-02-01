package com.omar.pcconnector.network.detection



// a class which implements this interface is able to
// find running servers using a particular way
interface DetectionStrategy {
    fun getAvailableHosts(): List<DetectedHost>
}