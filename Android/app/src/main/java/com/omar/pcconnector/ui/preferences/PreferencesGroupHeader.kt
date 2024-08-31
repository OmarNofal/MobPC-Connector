package com.omar.pcconnector.ui.preferences

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow


fun LazyListScope.groupHeader(modifier: Modifier, title: String) =
    item { PreferencesGroupHeader(modifier = modifier, title = title) }


@Composable
fun PreferencesGroupHeader(modifier: Modifier, title: String) {
    Text(
        modifier = modifier,
        text = title,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelMedium
    )
}
