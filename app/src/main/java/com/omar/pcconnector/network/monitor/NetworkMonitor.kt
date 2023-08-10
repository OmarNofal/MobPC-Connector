package com.omar.pcconnector.network.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.NotConnected)
    val networkStatus:
            StateFlow<NetworkStatus>
        get() = _networkStatus

    private val callback = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    init {

        val connectivityManager =
            context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

        connectivityManager.requestNetwork(callback, object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities == null) {
                    _networkStatus.value = NetworkStatus.NotConnected
                    return
                }
                emitNewConnection(networkCapabilities)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _networkStatus.value = NetworkStatus.NotConnected
            }

            override fun onUnavailable() {
                _networkStatus.value = NetworkStatus.NotConnected
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                emitNewConnection(networkCapabilities)
            }
        })
    }

    private fun emitNewConnection(networkCapabilities: NetworkCapabilities) {
        val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular =
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        if (isWifi) {
            _networkStatus.value = NetworkStatus.Wifi
        } else if (isCellular) {
            _networkStatus.value = NetworkStatus.Cellular
        } else {
            _networkStatus.value = NetworkStatus.NotConnected
        }
    }

}

sealed class NetworkStatus {
    object NotConnected : NetworkStatus()
    object Wifi : NetworkStatus()
    object Cellular : NetworkStatus()

}