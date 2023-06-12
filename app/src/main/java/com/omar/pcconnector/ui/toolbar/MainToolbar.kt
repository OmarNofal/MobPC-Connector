package com.omar.pcconnector.ui.toolbar


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.drawAnimatedBorder
import com.omar.pcconnector.ui.main.ToolbarViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainToolbar(
    viewModel: ToolbarViewModel = hiltViewModel(),
    onShowTransfers: () -> Unit,
    isTransferOngoing: Boolean
) {

    var menuShown by remember { mutableStateOf(false) }

    TopAppBar(title = {
        Text(text = viewModel.serverName)
    },
        colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        actions = {

            IconButton(onClick = onShowTransfers) {

                val colors = listOf(Color(0xFFED4264), Color(0xFFFFEDBC), Color(0xFFED4264))

                Icon(
                    imageVector = Icons.Rounded.SwapVerticalCircle,
                    contentDescription = "Actions",
                    if (isTransferOngoing)
                    Modifier.drawAnimatedBorder(2.dp, CircleShape, colors, durationMillis = 1000)
                    else Modifier
                )
            }

            IconButton(onClick = { menuShown = !menuShown }) {
                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Actions")
            }

            val onMenuDismiss = { menuShown = false }
            MainToolbarOverflow(
                onOpenBrowser = viewModel::openLinkInBrowser,
                onCopyClipboard = viewModel::copyToPCClipboard,
                onLockPC = viewModel::lockPC,
                onShutdownPC = { viewModel.shutdownPC(); onMenuDismiss()},
                showMenu = menuShown,
                onMenuDismiss = onMenuDismiss ,
            )
        }
    )


}


@Composable
fun MainToolbarOverflow(
    showMenu: Boolean = false,
    onMenuDismiss: () -> Unit,
    onLockPC: () -> Unit,
    onShutdownPC: () -> Unit,
    onOpenBrowser: (String, Boolean) -> Unit,
    onCopyClipboard: (String) -> Unit
) {



    var showURLDialog by remember { mutableStateOf(false) }
    var showClipboardDialog by remember { mutableStateOf(false) }

    Box {
    DropdownMenu(expanded = showMenu, onDismissRequest = onMenuDismiss) {


        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = "Open URL in Browser"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open URL in Browser")
                }
            },
            onClick = { showURLDialog = true }
        )

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ContentPaste,
                        contentDescription = "Copy to PC Clipboard"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy to PC Clipboard")
                }
            },
            onClick = { showClipboardDialog = true }
        )

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = "Lock PC")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Lock PC")
                }
            },
            onClick = { onLockPC(); onMenuDismiss(); }
        )

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = "Shutdown PC"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shutdown PC")
                }
            },
            onClick = { onShutdownPC(); onMenuDismiss(); }
        )
    }
        if (showURLDialog) {
            URLDialog(
                onDismiss = { showURLDialog = false },
                onConfirm = { url, incognito ->
                    onOpenBrowser(url, incognito)
                    onMenuDismiss()
                }
            )
        } else if (showClipboardDialog) {
            ClipboardDialog(onDismiss = { showClipboardDialog = false}, onConfirm = {text-> onCopyClipboard(text); onMenuDismiss()})
        }


    }
}

@Composable
private fun URLDialog(
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
private fun ClipboardDialog(
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
