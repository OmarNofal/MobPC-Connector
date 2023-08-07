package com.omar.pcconnector.ui.detection

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.DetectedDevice
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.model.PairedDevice


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = {
            Text(text = "Available Servers")
        },
            actions = {
                IconButton(onClick = viewModel::refresh) {
                    Icon(Icons.Default.Sync, contentDescription = "Refresh")
                }
            }
        )
        }
    ) { contentPadding ->
        Log.i("STATE TYPE", state.toString())
        MainContent(
            Modifier
                .fillMaxSize()
                .padding(contentPadding),
            state,
            onPasswordSubmit = viewModel::onPasswordSubmit,
            onVerificationCancel = viewModel::onCancelVerification,
            onConnectToServer = {
                viewModel.connectToNewDevice(it) //onConnectionSelected(it.toConnection())
            },
            onConnectToPairedDevice = {
                viewModel.connectToPairedDevice(it)
            }
        )
    }
}

@Composable
fun MainContent(
    modifier: Modifier,
    state: DetectionScreenState,
    onPasswordSubmit: (String) -> Unit,
    onVerificationCancel: () -> Unit,
    onConnectToServer: (DetectedDevice) -> Unit,
    onConnectToPairedDevice: (PairedDevice) -> Unit
) {
    val detectedDevices = state.detectedDevices
    val pairedDevices = state.pairedDevices

    VerificationDialog(
        state.currentlyVerifying?.deviceInfo,
        onVerificationCancel,
        onPasswordSubmit
    )

    Box(modifier) {

        LazyColumn {

            item {
                Header(Modifier.padding(start = 16.dp), "Paired Devices")
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Your paired devices will appear here")
                    }
                }
            }
            else {
                items(pairedDevices) {
                    DeviceRow(
                        modifier = Modifier.fillMaxWidth(),
                        deviceInfo = it.deviceInfo
                    ) { onConnectToPairedDevice(it) }
                }
            }

            item {
                Header(Modifier.padding(start = 16.dp), "Detected Devices")
            }

            if (detectedDevices.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No devices were found. Make sure you are on the same local network",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(detectedDevices) {
                    DeviceRow(
                        modifier = Modifier.fillMaxWidth(),
                        deviceInfo = it.deviceInfo
                    ) { onConnectToServer(it) }
                }
            }

        }


    }
}


@Composable
private fun Header(modifier: Modifier, title: String) {
    Text(modifier = modifier, text = title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
}

@Composable
fun VerificationDialog(
    deviceInfo: DeviceInfo?,
    onVerificationCancel: () -> Unit,
    onPasswordSubmit: (String) -> Unit,
) {
    if (deviceInfo == null) return
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onVerificationCancel,
        dismissButton = {
            TextButton(onClick = onVerificationCancel) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = { onPasswordSubmit(password) }) {
                Text(text = "Login")
            }
        },
        text = {
            Column {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    keyboardActions = KeyboardActions(
                        onDone = { onPasswordSubmit(password) }
                    ),
                    keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Done)
                )
                Spacer(Modifier.height(4.dp))
            }
        },
        title = {
            Text(text = "Enter password for ${deviceInfo.name}")
        }
    )
}