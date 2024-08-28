package com.omar.pcconnector.ui.preferences

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun PreferenceTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun PreferenceSubtitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall
    )
}