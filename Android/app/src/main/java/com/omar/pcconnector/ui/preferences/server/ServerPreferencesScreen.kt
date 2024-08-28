package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.ui.preferences.groupHeader


@Composable
fun ServerPreferencesScreen(
    serverPreferencesViewModel: ServerPreferencesViewModel = hiltViewModel(),
    onDeviceDeleted: () -> Unit,
    onBackPressed: () -> Unit
) {

    ServerPreferencesScreen(
        pairedDevice = serverPreferencesViewModel.pairedDevice,
        preferencesActions = serverPreferencesViewModel,
        onDeviceDeleted = onDeviceDeleted,
        onBackPressed = onBackPressed
    )

}


@Composable
fun ServerPreferencesScreen(
    pairedDevice: PairedDevice,
    preferencesActions: ServerPreferencesActions,
    onDeviceDeleted: () -> Unit,
    onBackPressed: () -> Unit
) {


    var isDeleteDialogShown by remember {
        mutableStateOf(false)
    }

    if (isDeleteDialogShown)
        DeleteServerDialog(
            serverName = pairedDevice.deviceInfo.name,
            onDelete = { preferencesActions.deleteDevice(); onDeviceDeleted() },
            onDismissRequest = { isDeleteDialogShown = false }
        )

    Scaffold(
        topBar = {
            TopBar(
                goBack = onBackPressed,
                onDelete = { isDeleteDialogShown = true })
        }
    ) {

        LazyColumn(
            modifier = Modifier
                .padding(it)
                .imePadding()
        ) {

            header(pairedDevice.deviceInfo.name)

            groupHeader(
                modifier = Modifier.padding(start = 16.dp, top = 32.dp),
                title = "Server Information"
            )

            serverInfoGroup(pairedDevice)

            groupHeader(
                modifier = Modifier.padding(start = 16.dp, top = 32.dp),
                title = "Preferences"
            )

            singleServerPreferencesGroup(
                pairedDevice.deviceInfo.id,
                preferencesActions
            )

        }

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    goBack: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    TopAppBar(title = { /*TODO*/ }, navigationIcon = {
        IconButton(onClick = goBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back"
            )
        }
    }, actions = {
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Delete Server",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
    )
}