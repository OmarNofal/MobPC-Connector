package com.omar.pcconnector.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.preferences.UserPreferences.AppTheme


fun LazyListScope.displayGroup(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) =
    item {
        ThemeOption(currentTheme = currentTheme, onThemeSelected)
    }

@Composable
fun ThemeOption(currentTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {

    var isDialogOpen by remember {
        mutableStateOf(false)
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { isDialogOpen = true }
        .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

        Icon(
            modifier = Modifier.size(28.dp),
            imageVector = Icons.Rounded.Brightness4,
            contentDescription = "Theme Icon"
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(text = "Theme", style = MaterialTheme.typography.labelLarge)
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = currentTheme.toText(),
                style = MaterialTheme.typography.bodySmall
            )
        }

    }

    if (isDialogOpen) {
        ThemeDialog(currentTheme = currentTheme, onThemeSelected) {
            isDialogOpen = false
        }
    }

}


@Composable
fun ThemeDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        icon = {
            Icon(
                imageVector = Icons.Rounded.Brightness4,
                contentDescription = "Icon"
            )
        },
        title = { Text(text = "Theme") },
        text = {
            Column {
                for (i in AppTheme.entries) {
                    if (i == AppTheme.UNRECOGNIZED) continue
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                            .clip(
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onThemeSelected(i) }
                            .padding(10.dp)

                    ) {
                        RadioButton(
                            selected = currentTheme == i,
                            onClick = null
                        )
                        Text(
                            text = i.toText(),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        })
}

private fun AppTheme.toText() = when (this) {
    AppTheme.SYSTEM -> "Follow System Settings"
    AppTheme.DARK -> "Dark"
    AppTheme.LIGHT -> "Light"
    AppTheme.UNRECOGNIZED -> "Light"
}