//package com.omar.pcconnector.ui.detection
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.omar.pcconnector.data.DevicesRepository
//import com.omar.pcconnector.model.DetectedDevice
//import com.omar.pcconnector.model.PairedDevice
//import com.omar.pcconnector.network.connection.Connectivity
//import com.omar.pcconnector.ui.nav.Navigator
//import com.omar.pcconnector.ui.nav.ServerScreen
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//
//@HiltViewModel
//class DetectionViewModel @Inject constructor(
//    private val connectivity: Connectivity,
//    private val devicesRepository: DevicesRepository,
//    private val navigator: Navigator
//) : ViewModel() {
//
//
//    val state: StateFlow<DetectionScreenState>
//        get() = _state
//    private val _state: MutableStateFlow<DetectionScreenState> =
//        MutableStateFlow(DetectionScreenState())
//
//
//    init {
//        viewModelScope.launch {
//            val pairedDevices = devicesRepository.getAllPairedDevices()
//            val detectedHosts = _state.value.detectedDevices
//            _state.value =
//                _state.value.copy(
//                    detectedDevices = detectedHosts
//                        .filter { detectedDevice -> detectedDevice.deviceInfo.id !in pairedDevices.map { it.deviceInfo.id } },
//                    pairedDevices = pairedDevices,
//                    isSearching = false
//                )
//        }
//    }
//
//
//    init {
//        refreshAvailableServers()
//    }
//
//
//    fun connectToPairedDevice(pairedDevice: PairedDevice) {
//        navigator.navigate(ServerScreen.navigationCommand(pairedDevice.deviceInfo.id))
//    }
//
//    fun connectToNewDevice(detectedDevice: DetectedDevice) {
//        _state.value = _state.value.copy(currentlyVerifying = detectedDevice)
//    }
//
//
//    fun onCancelVerification() {
//        _state.value = _state.value.copy(currentlyVerifying = null)
//    }
//
//    fun refresh() {
//        refreshAvailableServers()
//    }
//
//    private fun stateToSearching() {
//        _state.value = _state.value.copy(isSearching = true)
//    }
//
//    private fun stateToConnecting(detectedDevice: DetectedDevice) {
//        _state.value =
//            _state.value.copy(connectingToDeviceId = detectedDevice.deviceInfo.id)
//    }
//
//    private fun refreshAvailableServers() {
//
//        viewModelScope.launch {
//            stateToSearching()
//            val detectedHosts =
//                connectivity.getDetectedServersOnLocalNetwork()
//            val pairedDevices = _state.value.pairedDevices
//            _state.value =
//                _state.value.copy(
//                    detectedDevices = detectedHosts
//                        .filter { detectedDevice -> detectedDevice.deviceInfo.id !in pairedDevices.map { it.deviceInfo.id } },
//                    pairedDevices = pairedDevices,
//                    isSearching = false
//                )
//        }
//
//    }
//
//}
//
//
//data class DetectionScreenState(
//    val detectedDevices: List<DetectedDevice> = listOf(),
//    val pairedDevices: List<PairedDevice> = listOf(),
//    val isSearching: Boolean = false,
//    val connectingToDeviceId: String? = null,
//    val currentlyVerifying: DetectedDevice? = null
//)