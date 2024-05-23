package com.omar.pcconnector.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Window
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.model.PairedDevice


@Composable
fun AppDrawer(
    modifier: Modifier,
    pairedDevices: List<PairedDevice>,
    selectedDeviceId: String,
    drawerState: DrawerState,
    onDeviceClicked: (String) -> Unit,
    onSettingsClicked: () -> Unit,
    content: @Composable () -> Unit
) {

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "My Servers",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 12.dp,
                        end = 4.dp,
                        bottom = 10.dp
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    pairedDevices.forEach {

                        val isSelected = it.deviceInfo.id == selectedDeviceId

                        DeviceRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 8.dp,
                                    end = 8.dp,
                                    top = 10.dp,
                                    bottom = 10.dp
                                )
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.1f
                                    ) else Color.Transparent
                                )
                                .clickable { onDeviceClicked(it.deviceInfo.id) }
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                            device = it,
                            isSelected = isSelected
                        )

                        if (it != pairedDevices.last()) {
                            HorizontalDivider()
                        }

                    }
                }

                HorizontalDivider()

                SettingsButton(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSettingsClicked() }
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                )


            }
        },
        content = content
    )

}


@Composable
private fun DeviceRow(
    modifier: Modifier,
    device: PairedDevice,
    isSelected: Boolean = false
) {


    val alpha = if (isSelected) 1.0f else 0.5f

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {

        Icon(
            imageVector = Icons.Rounded.Window,
            contentDescription = "Windows",
            tint = Color(0xff08a1f7).copy(alpha = alpha)
        )

        val fontWeight =
            if (isSelected) FontWeight.SemiBold else FontWeight.Normal

        Text(
            text = device.deviceInfo.name,
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha),
            fontWeight = fontWeight
        )
    }

}

@Composable
fun SettingsButton(
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = "Settings"
        )


        Text(
            text = "Settings",
            modifier = Modifier.padding(start = 12.dp, end = 6.dp),
            style = MaterialTheme.typography.titleMedium,
        )

    }
}

@Preview
@Composable
fun DeviceRowPreview() {
    DeviceRow(
        modifier = Modifier,
        device = PairedDevice(
            DeviceInfo("213", "My Device", "Windows"),
            "",
            "",
            false
        )
    )
}