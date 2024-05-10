package com.omar.pcconnector.ui.detection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.omar.pcconnector.model.DeviceInfo


@Composable
fun DeviceRow(
    modifier: Modifier,
    deviceInfo: DeviceInfo,
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
            Text(text = deviceInfo.name)
            Text(
                text = deviceInfo.os,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
    }

}