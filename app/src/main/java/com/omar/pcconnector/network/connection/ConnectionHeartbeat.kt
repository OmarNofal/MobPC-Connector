package com.omar.pcconnector.network.connection

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.omar.pcconnector.network.api.StatusAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


/**
 * This class is responsible to monitor connection to a server
 * to make sure it is still accessible
 */
class ConnectionHeartbeat(connection: Connection) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    private val _state = MutableStateFlow(State.UNKNOWN)
    val state: StateFlow<State> get() = _state

    private val api = connection.retrofit.create(StatusAPI::class.java)


    /**
     * Start monitoring the connection
     */
    fun start() {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                try {
                    val response = api.status().execute()
                    val jsonResponse = JsonParser.parseString(response.body()!!.string())
                    if ((jsonResponse as JsonObject).has("ip")) {
                        Log.i("NETWORK", "Server pinged successfully")
                    }
                    _state.value = State.AVAILABLE
                } catch (e: Exception) {
                    _state.value = State.UNAVAILABLE
                }
                delay(SYNC_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop monitoring the connection and set the state to unknown
     */
    fun stop() {
        _state.value = State.UNKNOWN
        job?.cancel()
    }

    enum class State {
        AVAILABLE, UNAVAILABLE, UNKNOWN
    }

    companion object {
        private const val SYNC_INTERVAL_MS = 4000L
    }


}