package com.omar.pcconnector.ui.preferences

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.preferences.LocalUserPreferences
import com.omar.pcconnector.preferences.UserPreferences
import com.omar.pcconnector.preferences.UserPreferences.AppTheme


@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = hiltViewModel(),
    openDrawer: () -> Unit,
    onDeviceClicked: (PairedDevice) -> Unit
) {


    PreferencesScreen(
        pairedDevices = viewModel.pairedDevices,
        openDrawer = openDrawer,
        setAppTheme = viewModel::setTheme,
        onDeviceClicked = onDeviceClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencesScreen(
    pairedDevices: List<PairedDevice>,
    openDrawer: () -> Unit,
    setAppTheme: (AppTheme) -> Unit,
    onDeviceClicked: (PairedDevice) -> Unit
) {

    val currentPrefs = LocalUserPreferences.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menu"
                        )
                    }
                })
        }
    ) {

        LazyColumn(Modifier.padding(it)) {

            space(modifier = Modifier.height(16.dp))

            groupHeader(
                modifier = Modifier.padding(start = 16.dp),
                title = "Display"
            )

            displayGroup(
                currentTheme = currentPrefs.appTheme,
                setAppTheme
            )

            groupHeader(
                modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                title = "Servers Preferences"
            )

            serversPreferencesGroup(
                pairedDevices,
                onDeviceClicked = onDeviceClicked
            )
        }

    }
}

fun LazyListScope.space(modifier: Modifier) =
    item { Spacer(modifier = modifier) }

@Preview
@Composable
private fun PreviewPrefsScreen() {
    CompositionLocalProvider(LocalUserPreferences provides UserPreferences.getDefaultInstance()) {
        PreferencesScreen(
            pairedDevices = listOf(),
            setAppTheme = {},
            openDrawer = {},
            onDeviceClicked = {}
        )
    }
}