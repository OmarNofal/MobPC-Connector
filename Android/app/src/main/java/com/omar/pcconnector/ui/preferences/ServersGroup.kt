package com.omar.pcconnector.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.R
import com.omar.pcconnector.model.PairedDevice


fun LazyListScope.serversPreferencesGroup(
    pairedDevices: List<PairedDevice>,
    onDeviceClicked: (PairedDevice) -> Unit
) {
    items(pairedDevices) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDeviceClicked(it) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                modifier = Modifier.size(28.dp),
                painter = painterResource(id = R.drawable.windows_icon),
                contentDescription = "Icon",
                tint = Color.Unspecified
            )

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = it.deviceInfo.name,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = it.deviceInfo.os,
                    style = MaterialTheme.typography.bodySmall
                )
            }

        }
    }
}

