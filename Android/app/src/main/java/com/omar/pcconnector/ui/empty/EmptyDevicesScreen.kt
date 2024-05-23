package com.omar.pcconnector.ui.empty

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.R


/**
 * Screen shown when the user has not paired
 * with any devices
 */
@Composable
fun EmptyDevicesScreen(
    modifier: Modifier,
    onStartPairing: () -> Unit,
) {


    Scaffold(modifier) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        )
        {

            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.empty_screen_icon),
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(128.dp)
                )

                Text(
                    text = "Pair with your PC to get started",
                    modifier = Modifier.padding(top = 32.dp),
                    style = MaterialTheme.typography.titleMedium,

                )

                Button(
                    onClick = onStartPairing,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode,
                        contentDescription = "QR"
                    )
                    Text(text = "Start Pairing", modifier = Modifier.padding(start = 8.dp))
                }

            }

        }

    }


}