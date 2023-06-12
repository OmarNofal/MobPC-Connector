package com.omar.pcconnector.ui.detection

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.network.detection.DetectedHost


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    onSwitchScreen: () -> Unit,
    viewModel: DetectionViewModel = hiltViewModel()
) {

    val state by viewModel.availableServers.collectAsState(initial = DetectionScreenState.NoServers)
    Log.i("State Change", state.toString())


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
        MainContent(Modifier.padding(contentPadding), state) { viewModel.connectToServer(it); onSwitchScreen() }

    }
}

@Composable
fun MainContent(
    modifier: Modifier,
    state: DetectionScreenState,
    onConnectToServer: (DetectedHost) -> Unit
) {
    when (state) {
        DetectionScreenState.NoServers -> {
            Box(modifier = modifier
                .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No Available Servers ╯︿╰", color = Color.Red)
            }
        }

        DetectionScreenState.NoNetwork -> {
            Box(modifier = modifier
                .fillMaxSize()) {
                Text(text = "No Network Connection", color = Color.Blue)
            }
        }

        is DetectionScreenState.AvailableServers -> {
            LazyColumn(
                modifier
                    .fillMaxSize()) {
                items(state.servers) {
                    DetectedServerRow(modifier = Modifier.fillMaxWidth(), detectedHost = it) {
                        onConnectToServer(it)
                    }
                }
            }
        }
    }
}

