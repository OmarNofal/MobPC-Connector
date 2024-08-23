package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.R


fun LazyListScope.header(
    serverName: String
) = item {
    ServerPrefsHeader(
        modifier = Modifier.fillMaxWidth(),
        serverName = serverName
    )
}

@Composable
private fun ServerPrefsHeader(modifier: Modifier, serverName: String) {

    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            modifier = Modifier.size(64.dp),
            painter = painterResource(id = R.drawable.windows_icon),
            contentDescription = "Icon",
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = serverName, style = MaterialTheme.typography.headlineSmall)

    }

}