package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.preferences.LocalUserPreferences
import com.omar.pcconnector.preferences.ServerPreferences


fun LazyListScope.singleServerPreferencesGroup(
    deviceId: String,
    onChangeStartingPath: (String) -> Unit
) = item {
    SingleServerPreferencesGroup(
        Modifier
            .fillMaxWidth(),
        deviceId,
        onChangeStartingPath
    )
}

@Composable
private fun SingleServerPreferencesGroup(
    modifier: Modifier,
    serverId: String,
    onChangeStartingPath: (String) -> Unit
) {

    val preferences = LocalUserPreferences.current
    val serverPreferences = remember(preferences) {
        preferences.serversPreferencesList.find { it.serverId == serverId }
            ?: ServerPreferences.newBuilder().setServerId(serverId)
                .setStartPath("").build()
    }

    Column(modifier) {

        StartPathPreference(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            value = serverPreferences.startPath,
            onValueChange = onChangeStartingPath
        )


    }

}

@Composable
private fun StartPathPreference(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier
    ) {

        Text(
            text = "Starting Path",
            style = MaterialTheme.typography.labelLarge
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            value = value,
            onValueChange = onValueChange
        )

    }
}