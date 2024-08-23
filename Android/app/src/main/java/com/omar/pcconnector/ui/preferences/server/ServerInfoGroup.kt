package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.model.PairedDevice
import okhttp3.internal.trimSubstring


fun LazyListScope.serverInfoGroup(serverInfo: PairedDevice) =
    item {
        ServerInfoGroup(
            modifier = Modifier.fillMaxWidth(),
            serverInfo = serverInfo
        )
    }

@Composable
fun ServerInfoGroup(modifier: Modifier, serverInfo: PairedDevice) {

    val rowModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)

    Column(modifier) {

        InfoRow(
            modifier = rowModifier,
            title = "Name",
            value = serverInfo.deviceInfo.name
        )

        InfoRow(
            modifier = rowModifier,
            title = "ID",
            value = serverInfo.deviceInfo.id.drop(20)
        )

        InfoRow(
            modifier = rowModifier,
            title = "Operating System",
            value = serverInfo.deviceInfo.os.osToHumanReadable()
        )


    }

}

@Composable
private fun InfoRow(
    modifier: Modifier,
    title: String,
    value: String
) {

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(text = title, style = MaterialTheme.typography.labelLarge)

        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

    }

}

private fun String.osToHumanReadable() =
    when (this) {
        "win32" -> "Windows"
        else -> "Unknown"
    }
