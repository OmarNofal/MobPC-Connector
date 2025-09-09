package com.omar.pcconnector.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.omar.pcconnector.preferences.UserPreferences.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val preferences = dataStore.data
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            runBlocking { dataStore.data.first() }) // this will load it synchronously

    fun getServerPreferences(serverId: String): ServerPreferences =
        preferences.value.serversPreferencesList.find { it.serverId == serverId }
            ?: ServerPreferences.getDefaultInstance()

    fun setTheme(appTheme: AppTheme) {
        updateData {
            it.toBuilder().setAppTheme(appTheme).build()
        }
    }

    fun setServerAsDefault(serverId: String) {
        updateData {
            it.toBuilder().setDefaultServerId(serverId).build()
        }
    }

    fun setServerStartPath(serverId: String, startPath: String) {
        updateServerPreferences(serverId) {
            it.toBuilder().setStartPath(startPath).build()
        }
    }


    fun toggleShowHiddenResource(serverId: String) {
        updateServerPreferences(serverId) {
            it.toBuilder().setShowHiddenResources(!it.showHiddenResources)
                .build()
        }
        Log.d("Server id", serverId)
    }

    fun changeFileSystemSortCriteria(
        serverId: String,
        displayOrder: ServerPreferences.FileSystemSortCriteria
    ) {
        updateServerPreferences(serverId) {
            it.toBuilder().setSortingCriteria(displayOrder).build()
        }
    }

    fun changeFilesAndFoldersSeparation(
        serverId: String,
        value: ServerPreferences.FoldersAndFilesSeparation
    ) {
        updateServerPreferences(serverId) {
            it.toBuilder().setFoldersAndFilesSeparation(value).build()
        }
    }

    fun toggleSendPhoneNotifications(
        serverId: String
    ) {
        updateServerPreferences(serverId) {
            val oldValue = it.sendPhoneNotificationsToServer
            it.toBuilder().setSendPhoneNotificationsToServer(!oldValue).build()
        }
    }

    fun setNotificationsExcludedPackages(
        serverId: String,
        packageNames: List<String>
    ) {
        updateServerPreferences(serverId) {
            it.toBuilder().clearNotificationsExcludedPackages()
                .addAllNotificationsExcludedPackages(packageNames)
                .build()
        }
    }

    /**
     * Updates the preferences of a single server.
     * If the server is not found, then a new entry for the server is created
     * with default values
     */
    private fun updateServerPreferences(
        serverId: String,
        callback: (ServerPreferences) -> ServerPreferences
    ) {
        updateData {
            val serverPrefs =
                it.serversPreferencesList.firstOrNull { serverPreferences -> serverPreferences.serverId == serverId }

            val index = it.serversPreferencesList.indexOf(serverPrefs)

            return@updateData if (serverPrefs == null) {
                it.toBuilder().addServersPreferences(
                    callback(ServerPreferences.newBuilder().setServerId(serverId).build())
                ).build()
            } else {
                it.toBuilder().setServersPreferences(
                    index,
                    callback(serverPrefs)
                ).build()
            }
        }
    }

    private fun updateData(callback: (UserPreferences) -> UserPreferences) {
        scope.launch {
            dataStore.updateData(callback)
        }
    }

}