package com.omar.pcconnector.network.detection

import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.nio.charset.Charset


private const val PORT = 4285

/**
 * Class used to detect PCs that are running the server using the local network synchronously
 *
 * How this class works:
 *      1- It finds all the networks the device is connected to (WIFI, Mobile Data, Device Hotspot, ..etc)
 *      2- It sends an IP broadcast message to all devices in the network, stating that it is discovering servers
 *      3- If a server is present on a network, it will then respond with its status (name, ip, port)
 */
class DetectionLocalNetworkStrategy : DetectionStrategy {

    override fun getAvailableHosts(): List<DetectedHost> {

        val localNetworksIPs = getCurrentLocalNetworksBroadcast()

        val result = mutableListOf<DetectedHost>()
        runBlocking {
            val jobs = mutableListOf<Job>()
            for (networkBroadcast in localNetworksIPs) {
                val job = launch(Dispatchers.IO) {
                    result.addAll(checkDevicesFromNetwork(networkBroadcast))
                }
                jobs.add(job)
            }
            jobs.joinAll()
        }
        return result
    }

    private fun getIPByteArray(ip: String): ByteArray {
        val ipAddressInt = ipAddressStringToInt(ip)
        return byteArrayOf(
            ((ipAddressInt shr 24) and 0xFF).toByte(),
            ((ipAddressInt shr 16) and 0xFF).toByte(),
            ((ipAddressInt shr 8) and 0xFF).toByte(),
            ((ipAddressInt) and 0xFF).toByte()
        )
    }


    private fun checkDevicesFromNetwork(networkBroadcast: String): List<DetectedHost> {

        Log.i("DETECTION", "Finding hosts on $networkBroadcast")

        val socket = DatagramSocket()
        socket.broadcast = true
        socket.soTimeout = 300

        val queryData = "PC Connector Discovery".toByteArray()

        val addressByteArray = getIPByteArray(networkBroadcast)
        val packet = DatagramPacket(
            queryData,
            queryData.size,
            InetAddress.getByAddress(
                addressByteArray
            ),
            PORT
        )

        socket.send(packet)


        val detectedHosts = mutableListOf<DetectedHost>()

        try {
            while (true) {
                val receivedPacket = DatagramPacket(ByteArray(1000) { _ -> 0 }, 0, 1000)
                socket.receive(receivedPacket)

                val responseString = receivedPacket.data.takeWhile { b -> b != (0).toByte() }.toByteArray().toString(
                    Charset.forName("UTF-8"))
                Log.i("DETECTION", responseString)

                try {
                    val responseJson = JsonParser.parseString(responseString).asJsonObject
                    val name = responseJson.get("name").asString
                    val ip =
                        receivedPacket.address.hostAddress?.toString() ?: throw IllegalStateException("Invalid IP address from server")
                    val port = responseJson.get("port").asInt
                    detectedHosts.add(DetectedHost(name, ip, port))
                } catch (e: Exception) {
                    // invalid response from the server
                    Log.e("DETECTION", e.message.toString())
                    continue
                }
            }
        } catch (e: SocketTimeoutException) {
            return detectedHosts
        }
    }

    private fun ipAddressStringToInt(ip: String): Int {
        val ipNumbers = ip.split(".").map { it.toInt() }
        Log.i("Numbers", ipNumbers.toString())
        return (ipNumbers[0] shl 24) + (ipNumbers[1] shl 16) + (ipNumbers[2] shl 8) + (ipNumbers[3])
    }


    // get broadcast address of all local networks
    private fun getCurrentLocalNetworksBroadcast(): List<String> {
        val result = mutableListOf<String>()
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val n = networkInterfaces.nextElement()
            for (j in n.interfaceAddresses) {
                if (j.address.isSiteLocalAddress && j.broadcast != null) {
                    j.broadcast.hostAddress?.let { result.add(it) } ?: continue
                    Log.i("NET", "Found ${j.broadcast.hostAddress} && ${j.address.isSiteLocalAddress}")
                }
            }
        }

        return result
    }

}