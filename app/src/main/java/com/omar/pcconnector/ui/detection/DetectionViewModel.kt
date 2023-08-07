package com.omar.pcconnector.ui.detection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.getRetrofit
import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.network.api.AuthAPI
import com.omar.pcconnector.network.connection.Connectivity
import com.omar.pcconnector.operation.LoginOperation
import com.omar.pcconnector.ui.nav.Navigator
import com.omar.pcconnector.ui.nav.ServerScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val connectivity: Connectivity,
    private val devicesRepository: DevicesRepository,
    private val navigator: Navigator
) : ViewModel() {


    val state: StateFlow<DetectionScreenState>
        get() = _state
    private val _state: MutableStateFlow<DetectionScreenState> =
        MutableStateFlow(DetectionScreenState.Searching(listOf(), listOf()))

    // Whether we are currently asking the user for a password or not
    private val _verifiedState = MutableStateFlow<DetectedDevice?>(null)
    val verifiedState: StateFlow<DetectedDevice?>
        get() = _verifiedState



    init {
        refreshAvailableServers()
    }


    fun connectToPairedDevice(pairedDevice: PairedDevice) {
        navigator.navigate(ServerScreen.navigationCommand(pairedDevice.deviceInfo.id))
    }

    fun connectToNewDevice(detectedDevice: DetectedDevice) {
        _verifiedState.value = detectedDevice
    }

    fun onCancelVerification() {
        _verifiedState.value = null
    }

    fun onPasswordSubmit(password: String) {
        val detectedDevice = _verifiedState.value ?: return
        _verifiedState.value = null
        stateToConnecting(detectedDevice)

        viewModelScope.launch {

            try {
                val connection = getRetrofit(detectedDevice.ip, detectedDevice.port)
                val token = LoginOperation(
                    api = connection.create(AuthAPI::class.java),
                    password = password
                ).start()

                Log.i("LOGIN", "Logged In $token")
                val pairedDevice = PairedDevice(detectedDevice.deviceInfo, token, false)
                devicesRepository.storeDevice(pairedDevice)

                if (!isActive) return@launch
                navigator.navigate(ServerScreen.navigationCommand(detectedDevice.deviceInfo.id))
            } catch (e: Exception) {
                Log.e("DETECTION_VIEW_MODEL", "Failed to login " + e.message + e::class.java)
            }
        }
    }

    fun refresh() {
        refreshAvailableServers()
    }

    private fun stateToSearching() {
        val detectedHosts = _state.value.detectedDevices
        val pairedDevices = _state.value.pairedDevices
        _state.value = DetectionScreenState.Searching(detectedHosts, pairedDevices)
    }

    private fun stateToConnecting(detectedDevice: DetectedDevice) {
        val detectedDevices = _state.value.detectedDevices
        val pairedDevices = _state.value.pairedDevices
        _state.value = DetectionScreenState.Connecting(detectedDevices, pairedDevices, detectedDevice.deviceInfo.id)
    }

    private fun refreshAvailableServers() {

        viewModelScope.launch {
            stateToSearching()
            val detectedHosts =
                connectivity.getDetectedServersOnLocalNetwork()

            _state.value =
                DetectionScreenState.Idle(
                    detectedHosts, listOf()
                )
        }

    }

}


sealed class DetectionScreenState(
    val detectedDevices: List<DetectedDevice>,
    val pairedDevices: List<PairedDevice>
) {

    class Searching(
        detectedDevices: List<DetectedDevice>,
        pairedDevices: List<PairedDevice>
    ) : DetectionScreenState(detectedDevices, pairedDevices)

    class Idle(
        detectedDevices: List<DetectedDevice>,
        pairedDevices: List<PairedDevice>
    ) : DetectionScreenState(detectedDevices, pairedDevices)

    class Connecting(
        detectedDevices: List<DetectedDevice>,
        pairedDevices: List<PairedDevice>,
        val deviceId: String
    ): DetectionScreenState(detectedDevices, pairedDevices)

}
