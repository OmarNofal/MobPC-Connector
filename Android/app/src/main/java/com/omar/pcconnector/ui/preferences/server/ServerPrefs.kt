package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.preferences.LocalUserPreferences
import com.omar.pcconnector.preferences.ServerPreferences


fun LazyListScope.singleServerPreferencesGroup(
    deviceId: String,
    onChangeStartingPath: (String) -> Unit,
    onSetAsDefault: () -> Unit
) = item {
    SingleServerPreferencesGroup(
        Modifier
            .fillMaxWidth(),
        deviceId,
        onChangeStartingPath,
        onSetAsDefault
    )
}

@Composable
private fun SingleServerPreferencesGroup(
    modifier: Modifier,
    serverId: String,
    onChangeStartingPath: (String) -> Unit,
    onSetAsDefault: () -> Unit
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

        DefaultServerPreference(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            isDefault = preferences.defaultServerId == serverId,
            onSetAsDefault = onSetAsDefault
        )

    }

}

@Composable
private fun StartPathPreference(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {

    val text = remember(value) {
        TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        )
    }

    Column(
        modifier
    ) {

        Text(
            text = "Starting Path",
            style = MaterialTheme.typography.labelLarge
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            value = text,
            onValueChange = { onValueChange(it.text) }
        )

    }
}

@Composable
private fun DefaultServerPreference(
    modifier: Modifier,
    isDefault: Boolean,
    onSetAsDefault: () -> Unit
) {

    Row(
        modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {
            Text(text = "Is default server?", style = MaterialTheme.typography.labelLarge)
            Text(text = "Auto-connect when app starts", style = MaterialTheme.typography.labelSmall)
        }

        if (isDefault) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0.0f, 0.8f, 0.0f)
            )
        }
        else {
            Button(onClick = onSetAsDefault) {
                Text(text = "Set as Default")
            }
        }

    }

}