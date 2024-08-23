package com.omar.pcconnector.ui.preferences.server

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class ServerPreferencesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val devicesRepository: DevicesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {


    val serverId = savedStateHandle.get<String>("serverId") ?: ""

    lateinit var pairedDevice: PairedDevice

    init {

        run {
            if (serverId.isBlank()) return@run

            pairedDevice =
                runBlocking { devicesRepository.getPairedDevice(serverId) }
        }
    }

    fun setStartPath(startPath: String) {
        userPreferencesRepository.setServerStartPath(serverId, startPath)
    }

    fun deleteServer() {
        devicesRepository.deleteDevice(serverId)
    }


}