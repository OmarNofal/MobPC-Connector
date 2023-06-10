package com.omar.pcconnector.ui.detection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.network.connection.AppConnectivity
import com.omar.pcconnector.network.detection.DetectedHost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetectionViewModel @Inject constructor(
    //@ApplicationContext val appContext: Context,
    private val appConnectivity: AppConnectivity
) : ViewModel() {


    val availableServers: Flow<DetectionScreenState>
            get() = _availableServers.onEach { Log.i("State Changed", it.toString()) }
    private val _availableServers: MutableStateFlow<DetectionScreenState>
        = MutableStateFlow(DetectionScreenState.NoServers)


    private var flowJob: Job? = null

    init {
        refreshAvailableServers()
    }


    fun connectToServer(detectedHost: DetectedHost) {
        appConnectivity.connectToServer(detectedHost)
        //TODO("Redirect to the main page")
    }

    fun refresh() {
        refreshAvailableServers()
    }

    private fun refreshAvailableServers() {
        flowJob?.cancel()
        _availableServers.value = DetectionScreenState.NoServers
        flowJob = viewModelScope.launch {
            appConnectivity.getListOfAvailableServers()
                .catch {   // assume the error is always network-related
                    _availableServers.value = DetectionScreenState.NoNetwork
                }
                .collect{ detectedServers ->
                    Log.i("Detect", "Collected flow of detected devices")
                    if (detectedServers.isEmpty())
                        _availableServers.value = DetectionScreenState.NoServers
                    else
                        _availableServers.value = DetectionScreenState.AvailableServers(
                            detectedServers
                        )
                }
        }
    }

}




sealed class DetectionScreenState {

    object NoNetwork: DetectionScreenState()
    object NoServers: DetectionScreenState()
    class AvailableServers(
        val servers: List<DetectedHost>
    ): DetectionScreenState()

}