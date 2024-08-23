package com.omar.pcconnector.ui.preferences.server

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


@Composable
fun DeleteServerDialog(
    serverName: String,
    onDelete: () -> Unit, onDismissRequest: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        DialogButton(text = "Confirm", onClick = onDelete)
    }, dismissButton = {
        DialogButton(text = "Cancel", onClick = onDismissRequest)
    }, text = { Text(text = "Delete $serverName?") }, icon = {
        Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = null
        )
    })
}

@Composable
private fun DialogButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = text)
    }
}