package com.omar.pcconnector.ui.preferences.server

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotificationsExcludedPackagesViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val serverId = savedStateHandle.get<String>("serverId")
        ?: throw IllegalArgumentException("Server ID not provided")


    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val state: StateFlow<ScreenState>
        get() = _state

    init {
        loadInitialState()
    }

    private fun loadInitialState() {

        viewModelScope.launch(Dispatchers.IO) {
            val packages = getAllPackages()
            val excludedPackageNames =
                userPreferencesRepository.getServerPreferences(serverId).notificationsExcludedPackagesList

            _state.value = ScreenState.Loaded(packages, excludedPackageNames)
        }

    }

    fun removePackage(packageName: String) {
        _state.update {
            val it = it as ScreenState.Loaded
            ScreenState.Loaded(
                allPackages = it.allPackages,
                excludedPackageNames = it.excludedPackageNames.filter { it != packageName }
            )
        }
    }

    fun addPackage(packageName: String) {
        _state.update {
            val it = it as ScreenState.Loaded
            it.copy(
                excludedPackageNames = it.excludedPackageNames.filter { it != packageName } + packageName
            )
        }
    }

    fun submitChanges() {
        val state = _state.value as ScreenState.Loaded
        userPreferencesRepository.setNotificationsExcludedPackages(
            serverId,
            state.excludedPackageNames
        )
    }

    private fun getAllPackages(): List<Package> {

        val pm = context.packageManager

        val installedPackages =
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .also { Log.d("Packages", it.size.toString()) }.filter {
                    true//it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }
        return installedPackages.map {

            val packageName = it.packageName
            val appName = it.loadLabel(pm).toString()
            val icon = it.loadIcon(pm)

            Package(appName, icon, packageName)
        }
    }

    sealed class ScreenState {

        data object Loading : ScreenState()

        data class Loaded(
            val allPackages: List<Package>,
            val excludedPackageNames: List<String>
        ) : ScreenState()
    }

    data class Package(
        val appName: String,
        val icon: Drawable,
        val packageName: String
    )
}