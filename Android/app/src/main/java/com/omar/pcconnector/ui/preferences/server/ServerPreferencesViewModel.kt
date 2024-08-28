package com.omar.pcconnector.ui.preferences.server

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.preferences.ServerPreferences
import com.omar.pcconnector.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class ServerPreferencesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val devicesRepository: DevicesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel(), ServerPreferencesActions {


    val serverId = savedStateHandle.get<String>("serverId") ?: ""

    lateinit var pairedDevice: PairedDevice

    init {

        run {
            if (serverId.isBlank()) return@run

            pairedDevice =
                runBlocking { devicesRepository.getPairedDevice(serverId) }
        }
    }

    override fun setStartPath(path: String) {
        userPreferencesRepository.setServerStartPath(serverId, path)
    }

    override fun deleteDevice() {
        devicesRepository.deleteDevice(serverId)
    }

    override fun setAsDefault() {
        userPreferencesRepository.setServerAsDefault(serverId)
    }

    override fun toggleShowHiddenResource() {
        userPreferencesRepository.toggleShowHiddenResource(serverId)
    }

    override fun changeFileSystemSortCriteria(displayOrder: ServerPreferences.FileSystemSortCriteria) {
        userPreferencesRepository.changeFileSystemSortCriteria(serverId, displayOrder)
    }

    override fun changeFilesAndFoldersSeparation(value: ServerPreferences.FoldersAndFilesSeparation) {
        userPreferencesRepository.changeFilesAndFoldersSeparation(serverId, value)
    }

}