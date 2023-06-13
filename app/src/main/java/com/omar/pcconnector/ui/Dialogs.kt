package com.omar.pcconnector.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp


@Composable
fun MakeDirDialog(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember {
        FocusRequester()
    }
    var dirName by remember {
        mutableStateOf("")
    }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "Create a Folder") },
        text = {

            Column {
                TextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = dirName,
                    onValueChange = { dirName = it })
                Spacer(Modifier.height(4.dp))
                Text(text = "Tip: Use / to create nested directories")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(dirName); onCancel() }) {
                Text(text = "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = "Cancel")
            }
        }
    )

    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }
}



@Composable
fun DeleteDialog(
    fileName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = "Cancel")
            }
        },
        text = { Text(text = "Delete $fileName?") }
    )
}


@Composable
fun RenameDialog(
    oldName: String,
    onConfirm: (String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var newName by remember {
        mutableStateOf(oldName)
    }
    var overwrite by remember {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "Enter New Name") },
        text = {

            Column {
                TextField(value = newName, onValueChange = { newName = it })
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Overwrite?")
                    Checkbox(checked = overwrite, onCheckedChange = { overwrite = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(newName, overwrite); onCancel() }) {
                Text(text = "Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = "Cancel")
            }
        }
    )
}


@Composable
fun URLDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit,

    ) {
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf(clipboardManager.getText()?.text ?: "http://") }
    var isIncognito by remember { mutableStateOf(false) }



    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enter URL") },
        text = {
            Column {
                TextField(value = inputText, onValueChange = {inputText = it} )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Incognito?")
                    Checkbox(checked = isIncognito, onCheckedChange = {isIncognito = it})
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(inputText, isIncognito); onDismiss() }) {
                Text(text = "Open")
            }},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun ClipboardDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf(clipboardManager.getText()?.text ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Enter Text") },
        text = {

            Column {
                TextField(value = inputText, onValueChange = {inputText = it}, placeholder = { Text("Enter text..." )})
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(inputText); onDismiss() }) {
                Text(text = "Send")
            }},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
