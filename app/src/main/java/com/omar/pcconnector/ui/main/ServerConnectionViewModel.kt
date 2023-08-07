package com.omar.pcconnector.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.network.connection.ServerConnection
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject



class ServerConnectionViewModel @AssistedInject constructor(
    @Assisted private val pairedDevice: PairedDevice
): ViewModel() {

    val serverConnection =
        ServerConnection(
            pairedDevice.deviceInfo.id,
            pairedDevice.token,
            viewModelScope
        )

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