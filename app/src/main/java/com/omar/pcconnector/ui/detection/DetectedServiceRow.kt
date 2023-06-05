package com.omar.pcconnector.ui.detection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omar.pcconnector.R
import com.omar.pcconnector.network.detection.DetectedHost


@Composable
fun DetectedServerRow(
    modifier: Modifier,
    detectedHost: DetectedHost,
    onClick: () -> Unit
) {

    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_computer_24),
            contentDescription = "Server",
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = detectedHost.serverName)
            Text(
                text = "${detectedHost.ipAddress}:${detectedHost.port}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
    }

}