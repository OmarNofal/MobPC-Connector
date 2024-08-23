package com.omar.pcconnector.ui.preferences

import androidx.lifecycle.ViewModel
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.preferences.UserPreferences
import com.omar.pcconnector.preferences.UserPreferences.AppTheme
import com.omar.pcconnector.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val devicesRepository: DevicesRepository
) : ViewModel() {


    val pairedDevices = runBlocking(Dispatchers.IO) { devicesRepository.getAllPairedDevices() }


    fun setTheme(appTheme: AppTheme) =
        userPreferencesRepository.setTheme(appTheme)

}