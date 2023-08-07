package com.omar.pcconnector.network.connection

import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.network.api.StatusAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
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
    private val id: String,
    private val token: String,
    private val scope: CoroutineScope
) {

    private val  _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Searching)
    val connectionStatus: StateFlow<ConnectionStatus>
        get() = _connectionStatus

    private var monitorJob: Job? = null

    init {
        startMonitoring()
    }

    /**
     * Find the device on the local or
     * global network.
     */
    private suspend fun findDevice(): DetectedDevice? = withContext(Dispatchers.IO) {
        val device = Connectivity.findDevice(id) ?: return@withContext null
        if (!isActive) return@withContext null
        return@withContext device
    }

    private fun startMonitoring() {
        monitorJob = scope.launch(Dispatchers.IO) {
            try {
                onStartSearching()
                val device = withTimeout(5000) { findDevice() }
                if (device == null) {
                    onConnectionNotFound()
                    return@launch
                } else {
                    val connection = device.toConnection(token)
                    onConnectionFound(connection)
                    while (isActive) {
                        if (!pingConnection(connection)) {
                            onConnectionLost()
                        }
                        delay(6000)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                onConnectionNotFound()
                return@launch
            }
        }
    }

    private suspend fun pingConnection(connection: Connection): Boolean = withContext(Dispatchers.IO) {
        val statusApi = connection.retrofit.create(StatusAPI::class.java)
        try { // TODO refactor this method to an operation
            statusApi.status().execute().body()
            true // if no errors, then we we reached the server successfully
        } catch (e: Exception) {
            false
        }
    }

    private fun onConnectionNotFound() {
        _connectionStatus.value = ConnectionStatus.NotFound
        monitorJob?.cancel()
        startMonitoring()
    }

    private fun onConnectionLost() {
        _connectionStatus.value = ConnectionStatus.NotFound
        monitorJob?.cancel()
        startMonitoring()
    }

    private fun onConnectionFound(connection: Connection) {
        _connectionStatus.value = ConnectionStatus.Connected(connection)
    }

    private fun onStartSearching() {
        _connectionStatus.value = ConnectionStatus.Searching
    }

}

sealed class ConnectionStatus {

    object NotFound: ConnectionStatus()

    class Connected(
        val connection: Connection
    ): ConnectionStatus()

    object Searching: ConnectionStatus()

}
