package com.omar.pcconnector

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.ui.theme.AppTheme
import com.omar.pcconnector.worker.ProcessTextWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
open class BaseProcessTextActivity : ComponentActivity() {

    @Inject
    lateinit var devicesRepository: DevicesRepository

    private var uiState by
    mutableStateOf<ProcessTextUiState>(ProcessTextUiState.LoadingDevices)

    protected var action: String = ProcessTextWorker.ACTION_COPY
    protected var data: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    when (val s = uiState) {
                        is ProcessTextUiState.LoadingDevices -> {
                            LoadingDevicesScreen()
                        }

                        is ProcessTextUiState.DevicesLoaded -> {
                            DevicesSelectionScreen(
                                s.devices,
                                this@BaseProcessTextActivity::onDeviceSelected
                            )
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            val devices = getAllDevices()
            if (devices.isEmpty()) {
                Toast.makeText(
                    this@BaseProcessTextActivity,
                    "Pair with a device first",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }
            if (devices.size == 1) onDeviceSelected(devices.first())
            else
                uiState = ProcessTextUiState.DevicesLoaded(devices)
        }
    }

    @Composable
    fun LoadingDevicesScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .background(Color(0xFF424242), RoundedCornerShape(4.dp))
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Loading your devices", color = Color.White)
            }
        }
    }

    @Composable
    fun DevicesSelectionScreen(
        devices: List<PairedDevice>,
        onDeviceSelected: (PairedDevice) -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LazyVerticalGrid(
                modifier = Modifier
                    .background(Color(0xFF424242), RoundedCornerShape(4.dp))
                    .padding(24.dp),
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                items(devices) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onDeviceSelected(it) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Computer,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(text = it.deviceInfo.name, fontSize = 18.sp, color = Color.White)
                        Text(text = it.deviceInfo.os, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }

    private fun onDeviceSelected(pairedDevice: PairedDevice) {
        // Submit the stuff for processing

        val workManager = WorkManager.getInstance(this)

        val actionWorkRequest =
            OneTimeWorkRequestBuilder<ProcessTextWorker>()
                //.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(
                    workDataOf(
                        ProcessTextWorker.ACTION_KEY to action,
                        ProcessTextWorker.DATA_KEY to data,
                        ProcessTextWorker.DEVICE_KEY to pairedDevice.deviceInfo.id
                    )
                ).build()
        workManager.enqueue(actionWorkRequest)

        finish()
    }

    private suspend fun getAllDevices(): List<PairedDevice> =
        devicesRepository.getAllPairedDevices()


    sealed interface ProcessTextUiState {
        object LoadingDevices : ProcessTextUiState
        class DevicesLoaded(val devices: List<PairedDevice>) : ProcessTextUiState
    }


}
