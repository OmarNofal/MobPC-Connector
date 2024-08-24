package com.omar.pcconnector.preferences

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
        updateData {
            val serverPrefs =
                it.serversPreferencesList.firstOrNull { it.serverId == serverId }

            val index = it.serversPreferencesList.indexOf(serverPrefs)

            return@updateData if (serverPrefs == null) {
                it.toBuilder().addServersPreferences(
                    ServerPreferences.newBuilder().setServerId(serverId)
                        .setStartPath(startPath).build()
                ).build()
            } else {
                it.toBuilder().setServersPreferences(
                    index,
                    serverPrefs.toBuilder().setStartPath(startPath)
                ).build()
            }

        }
    }


    fun updateData(callback: (UserPreferences) -> UserPreferences) {
        scope.launch {
            dataStore.updateData(callback)
        }
    }

}