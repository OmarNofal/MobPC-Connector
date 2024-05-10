package com.omar.pcconnector.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.network.connection.ConnectionStatus
import com.omar.pcconnector.network.connection.ServerConnection
import com.omar.pcconnector.network.monitor.NetworkMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow


class ServerConnectionViewModel @AssistedInject constructor(
    networkMonitor: NetworkMonitor,
    @Assisted private val pairedDevice: PairedDevice
) : ViewModel() {

    private val serverConnection =
        ServerConnection(
            pairedDevice.deviceInfo.id,
            pairedDevice.token,
            pairedDevice.certificate,
            viewModelScope,
            networkMonitor.networkStatus
        )


    val connectionStatus: StateFlow<ConnectionStatus>
        get() = serverConnection.connectionStatus

    fun searchAndConnect() = serverConnection.searchAndConnect()


    @AssistedFactory
    interface Factory {
        fun create(pairedDevice: PairedDevice): ServerConnectionViewModel
    }

    companion object {
        fun provideFactory(
            factory: Factory,
            pairedDevice: PairedDevice
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(pairedDevice) as T
            }
        }
    }

}