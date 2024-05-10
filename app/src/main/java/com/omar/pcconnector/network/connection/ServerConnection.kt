package com.omar.pcconnector.network.connection

import android.util.Log
import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.network.api.StatusAPI
import com.omar.pcconnector.network.monitor.NetworkStatus
import com.omar.pcconnector.operation.PingOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


/**
 * This class manages connection to a server
 * and tries to reconnect to the server whenever the connection
 * is down. Provides methods to access current connectivity status and
 * flows to monitor in real time.
 */
class ServerConnection(
    val id: String,
    private val token: String,
    private val certificate: String,
    private val scope: CoroutineScope,
    private val networkStatusFlow: StateFlow<NetworkStatus>,
) {

    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.Searching)
    val connectionStatus: StateFlow<ConnectionStatus>
        get() = _connectionStatus

    private var monitorJob: Job? = null
    private var searchJob: Job? = null

    init {
        searchAndConnect()
    }

    init {

        scope.launch {
            networkStatusFlow.collect {
                when (it) {
                    NetworkStatus.NotConnected -> handleDisconnection()
                    else -> handleNewNetwork()
                }
            }
        }
    }

    /**
     * Find the device on the local or
     * global network.
     */
    private suspend fun findDevice(): DetectedDevice? =
        withContext(Dispatchers.IO) {
            val device =
                Connectivity.findDevice(id, ConnectionPreference.LOCAL_NETWORK)
                    ?: return@withContext null
            if (!isActive) return@withContext null
            return@withContext device
        }

    /**
     * Starts the search process and returns immediately
     */
    fun searchAndConnect() {
        searchJob?.cancel()
        searchJob = scope.launch {
            onStartSearching()
            try {
                val device = withTimeout(5000) { findDevice() }
                    ?: return@launch if (isActive) onConnectionNotFound() else Unit
                if (isActive) {
                    onConnectionFound(device.toConnection(token, certificate))
                    Log.d(TAG, "Device found $device")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Device not found ${e.stackTraceToString()}")
                if (isActive)
                    onConnectionNotFound()
            }
        }
    }

    private fun handleDisconnection() {
        if (_connectionStatus.value is ConnectionStatus.Connected) {
            onConnectionLost()
        }
    }

    private fun handleNewNetwork() {
        searchAndConnect()
    }

    private fun monitorJob(connection: Connection): Job {
        return scope.launch {
            while (isActive) {
                if (!pingConnection(connection)) {
                    onConnectionLost()
                }
                delay(6000)
            }
        }
    }

    private suspend fun pingConnection(connection: Connection): Boolean =
        withContext(Dispatchers.IO) {
            val statusApi = connection.retrofit.create(StatusAPI::class.java)
            PingOperation(statusApi).start()
        }

    private fun onConnectionNotFound() {
        _connectionStatus.value = ConnectionStatus.NotFound
        monitorJob?.cancel()
    }

    private fun onConnectionLost() {
        monitorJob?.cancel()
        searchJob?.cancel()
        _connectionStatus.value = ConnectionStatus.NotFound
    }

    private fun onConnectionFound(connection: Connection) {
        Log.i("CONNECTION FOUND", connection.toString())
        _connectionStatus.value = ConnectionStatus.Connected(connection)
        monitorJob = monitorJob(connection)
        searchJob?.cancel()
    }

    private fun onStartSearching() {
        _connectionStatus.value = ConnectionStatus.Searching
    }

    companion object {
        const val TAG = "ServerConnection"
    }
}

sealed class ConnectionStatus {

    object NotFound : ConnectionStatus()

    class Connected(
        val connection: Connection
    ) : ConnectionStatus()

    object Searching : ConnectionStatus()

}
