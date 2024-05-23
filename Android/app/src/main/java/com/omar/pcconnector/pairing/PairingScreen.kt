package com.omar.pcconnector.pairing

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PairingScreen(
    viewModel: PairingScreenViewModel = hiltViewModel(),
    onPairedWithDevice: (id: String) -> Unit,
    onNavigateBack: () -> Unit
) {

    val state by viewModel.state.collectAsState()

    val scope = rememberCoroutineScope()

    val cameraPermission =
        rememberPermissionState(permission = Manifest.permission.CAMERA) {
            if (!it) {
                onNavigateBack()
            }
        }

    if (cameraPermission.status.isGranted) {
        PairingScreen(state = state) {
            Log.d("QR CODE", it)

            scope.launch {
                val pairingResult = viewModel.onQrCodeDetected(it)
                if (pairingResult is PairingScreenViewModel.PairingResult.Success) {
                    Log.d("Pairing", "ASKRRRR")
                    onPairedWithDevice(pairingResult.serverId)
                }
            }
        }
    } else {
        PermissionDialog(onRequestPermission = { cameraPermission.launchPermissionRequest() })
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PairingScreen(
    state: PairingScreenState,
    onQrCodeDetected: (String) -> Unit,
) {

    var codeScanner by remember {
        mutableStateOf<CodeScanner?>(null)
    }

    if (state is PairingScreenState.Connecting) {

        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Devices,
                    contentDescription = null
                )
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(strokeCap = StrokeCap.Round)
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = "Connecting to ${state.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(text = "Scan QR Code") })
        }
    ) { padding ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = {
                val view = CodeScannerView(it).apply {
                    isAutoFocusButtonVisible = false
                    isFlashButtonVisible = true
                }
                codeScanner = CodeScanner(it, view)
                    .apply {
                        setDecodeCallback { onQrCodeDetected(it.text) }
                        formats = CodeScanner.TWO_DIMENSIONAL_FORMATS
                        isFlashEnabled = false
                        isAutoFocusEnabled = true
                        camera = CodeScanner.CAMERA_BACK
                        startPreview()
                    }
                view
            }
        ) {

        }
    }
}


@Composable
fun PermissionDialog(
    onRequestPermission: () -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        onRequestPermission()
    }
    Scaffold(Modifier.fillMaxSize()) { padding ->

    }
}