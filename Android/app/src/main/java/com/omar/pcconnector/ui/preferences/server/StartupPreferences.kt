package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.ui.preferences.PreferenceSubtitle
import com.omar.pcconnector.ui.preferences.PreferenceTitle


@Composable
fun ColumnScope.startupPreferencesGroup(
    optionModifier: Modifier,
    startupPath: String,
    isDefaultServer: Boolean,
    onStartupPathChange: (String) -> Unit,
    onSetAsDefault: () -> Unit
) {

    StartPathPreference(
        modifier = optionModifier,
        value = startupPath,
        onValueChange = onStartupPathChange
    )

    DefaultServerPreference(
        modifier = optionModifier,
        isDefault = isDefaultServer,
        onSetAsDefault = onSetAsDefault
    )

}

@Composable
private fun StartPathPreference(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {

    val text = remember(value) {
        TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        )
    }

    Column(
        modifier
    ) {

        Text(
            text = "Starting Path",
            style = MaterialTheme.typography.labelLarge
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            value = text,
            onValueChange = { onValueChange(it.text) }
        )

    }
}

@Composable
private fun DefaultServerPreference(
    modifier: Modifier,
    isDefault: Boolean,
    onSetAsDefault: () -> Unit
) {


    BasicOptionSkeleton(
        modifier = modifier,
        title = { PreferenceTitle(text = "Is default server?") },
        subtitle = { PreferenceSubtitle(text = "Auto-connect when app starts") }
    ) {
        if (isDefault) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0.0f, 0.8f, 0.0f)
            )
        } else {
            Button(onClick = onSetAsDefault) {
                Text(text = "Set as Default")
            }
        }

    }


}