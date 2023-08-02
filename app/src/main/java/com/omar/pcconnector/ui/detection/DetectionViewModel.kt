package com.omar.pcconnector.ui.detection

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.network.connection.Connectivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val connectivity: Connectivity
) : ViewModel() {


    val state: StateFlow<DetectionScreenState>
        get() = _state
    private val _state: MutableStateFlow<DetectionScreenState> =
        MutableStateFlow(DetectionScreenState.Searching(listOf(), listOf()))

    // Whether we are currently asking the user for a password or not
    private val _verifiedState = MutableStateFlow<DeviceInfo?>(null)
    val verifiedState: StateFlow<DeviceInfo?>
        get() = _verifiedState

    private var flowJob: Job? = null

    init {
        refreshAvailableServers()
    }


    fun connectToPairedDevice(pairedDevice: PairedDevice) {

    }

    fun connectToNewDevice(detectedDevice: DetectedDevice) {
        _verifiedState.value = detectedDevice.deviceInfo
    }

    fun onCancelVerification() {
        _verifiedState.value = null
    }

    fun onPasswordSubmit(password: String) {
        _verifiedState.value = null
    }

    fun refresh() {
        refreshAvailableServers()
    }

    private fun stateToSearching() {
        val detectedHosts = _state.value.detectedDevices
        val pairedDevices = _state.value.pairedDevices
        _state.value = DetectionScreenState.Searching(detectedHosts, pairedDevices)
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

        /*flowJob?.cancel()
        _availableServers.value = DetectionScreenState.NoServers
        flowJob = viewModelScope.launch {
            appConnectivity.getListOfAvailableServers()
                .catch {   // assume the error is always network-related
                    _availableServers.value = DetectionScreenState.NoNetwork
                }
                .collect { detectedServers ->
                    Log.i("Detect", "Collected flow of detected devices")
                    if (detectedServers.isEmpty())
                        _availableServers.value = DetectionScreenState.NoServers
                    else
                        _availableServers.value = DetectionScreenState.AvailableServers(
                            detectedServers
                        )
                }
        }*/
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
