package com.omar.pcconnector.network.connection

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.omar.pcconnector.network.api.StatusAPI
import com.omar.pcconnector.network.detection.DetectedHost
import com.omar.pcconnector.network.detection.DetectionLocalNetworkStrategy
import com.omar.pcconnector.network.detection.DetectionStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


/**
 * This class is responsible for handling connectivity to a server
 *
 * Its main duties are:
 *  1- Detect available servers and connect to one based on user's history
 *  2- Expose a Retrofit object that corresponds to the server's url so other components don't have to handle connections
 *  3- Try to detect when a server disconnects as soon as possible and notify other components
 *  4- Allow user to select a particular server and disconnect from the current one
 */
class AppConnectivity(
    private val prefs: SharedPreferences
) {


    // TODO switch detected servers to single source of truth
    //val detectedServers: Flow<List<DetectedHost>> get() = availableServers
    private val availableServers = MutableStateFlow<List<DetectedHost>>(listOf())



    /**
     * Job which periodically searches for servers and if it finds
     * the last previously connected server, it will automatically connect
     */
    private var searchJob: Job? = null

    /**
     * Job which periodically makes sure that the connection to the server is still alive
     * and if not, then it disconnects automatically
     */
    private var pingJob: Job? = null

    /**
     * The retrofit object corresponding to the current connected server
     */
    val currentConnection: StateFlow<Connection?>
        get() = connectionFlow.asStateFlow()
    private val connectionFlow = MutableStateFlow<Connection?>(null)


    /**
     * Connects to a particular server as chosen by the user
     */
    fun connectToServer(detectedHost: DetectedHost) {
        connectToServerImpl(detectedHost)
    }


    /**
     * Disconnects from currently connected server due to user action
     */
//    fun disconnect() {
//        removeLastConnectedFromSettings()
//        disconnectionImpl()
//    }


    /**
     * Gets list of detected webservers so the user can choose manually
     *
     * @return The detected running webservers
     */
    fun getListOfAvailableServers(): Flow<List<DetectedHost>> {
        val detectedHostsFlow = MutableStateFlow<List<DetectedHost>>(listOf())
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            val detectionStrategy: DetectionStrategy = DetectionLocalNetworkStrategy()
            detectedHostsFlow.value = detectionStrategy.getAvailableHosts()
            Log.i("Detect", "Detected servers " + availableServers.value)
        }
        return detectedHostsFlow
    }


    /**
     * Called by the app when a disconnection has been detected
     */
    private fun onDisconnectionDetected() {
        disconnectionImpl()
    }

    private fun disconnectionImpl() {
        connectionFlow.value = null
        pingJob?.cancel()
        startSearchJob()
    }


    /**
     * Saves the address of the last connected server, so when the app launches
     * it connects to it again if it finds it
     */
    private fun setLastConnectedSettings(ip: String, port: Int) {
        prefs.edit().apply {
            putString("CONN_IP", ip)
            putInt("CONN_PORT", port)
            apply()
        }
    }

    /**
     * Return the last server connected to if it exists
     *
     * @return Pair of ip and port or null if there is no history
     */
    private fun getLastConnectedServer(): Pair<String, Int>? {
        val lastConnectedIP = prefs.getString("CONN_IP", null) ?: return null
        val lastPort = prefs.getInt("CONN_PORT", 4567)
        return lastConnectedIP to lastPort
    }



    /**
     * Clears the address of the last connected server from the settings.
     * This happens when the user disconnects from the server by himself, which means
     * he does not want the app to automatically connect to that server again
     */
//    private fun removeLastConnectedFromSettings() {
//        prefs.edit().apply {
//            remove("CONN_IP")
//            remove("CONN_PORT")
//            apply()
//        }
//    }



    private fun connectToServerImpl(detectedHost: DetectedHost) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://${detectedHost.ipAddress}:${detectedHost.port}/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        connectionFlow.value = Connection(
            detectedHost.serverName,
            detectedHost.ipAddress,
            detectedHost.port,
            retrofit
        )
        setLastConnectedSettings(detectedHost.ipAddress, detectedHost.port)
        startPingJob() // make sure the server stays valid
        searchJob?.cancel()
    }


    /**
     * Periodically searches for available servers and connects to last connected one
     * automatically
     */
    private fun startSearchJob() {
        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val (lastConnectedIP, lastConnectedPort) = getLastConnectedServer() ?: return@launch
                    val detectedHosts = DetectionLocalNetworkStrategy().getAvailableHosts()
                    detectedHosts.forEach {
                        if (it.ipAddress == lastConnectedIP && it.port == lastConnectedPort) {
                            connectToServerImpl(it)
                        }
                    }
                } catch (e: Exception) { // catch any exception for now

                }
                delay(5000) // delay for a bit before re-checking
            }
        }
    }

    /**
     * Periodically makes sure the server is alive, so we update the retrofit object accordingly
     */
    private fun startPingJob() {
        pingJob?.cancel()
        val retrofit = connectionFlow.value?.retrofit ?: return
        pingJob = CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            val statusAPI = retrofit.create<StatusAPI>()
            while (isActive) {
                try {
                    val response = statusAPI.status().execute()
                    val jsonResponse = JsonParser.parseString(response.body()!!.string())
                    if ((jsonResponse as JsonObject).has("ip")) {
                        Log.i("NETWORK", "Server pinged successfully")
                    }
                } catch (e: Exception) { // catch any exception for now and just disconnect
                    if (isActive) {
                        cancel() // server disconnected. cancel.
                        Log.e("NETWORK", "SERVER disconnected")
                        onDisconnectionDetected()
                    }
                }
                delay(5000) // delay for a bit before re-checking
            }
        }
    }


}
