package com.omar.pcconnector.ui.toolbar


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.drawAnimatedBorder
import com.omar.pcconnector.isGlobalIp
import com.omar.pcconnector.network.connection.ConnectionStatus
import com.omar.pcconnector.ui.ClipboardDialog
import com.omar.pcconnector.ui.URLDialog
import com.omar.pcconnector.ui.action.Actions
import com.omar.pcconnector.ui.action.ActionsDropdownMenu
import com.omar.pcconnector.ui.main.ToolbarViewModel
import com.omar.pcconnector.ui.theme.darkGreen
import com.omar.pcconnector.ui.theme.darkYellow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainToolbar(
    viewModel: ToolbarViewModel,
    onShowTransfers: () -> Unit,
    isTransferOngoing: Boolean
) {

    var menuShown by remember { mutableStateOf(false) }
    val connectionStatus by viewModel.connectionStatusFlow.collectAsState()

    TopAppBar(title = {
        Text(text = viewModel.serverName)
    },
        colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        actions = {


            val statusBackgroundColor = when (val s = connectionStatus) {
                is ConnectionStatus.Connected ->
                    if (s.connection.ip.isGlobalIp()) darkYellow else darkGreen
                else -> Color.Red
            }
            var connectionInfoShown by remember {
                mutableStateOf(false)
            }
            IconButton(onClick = { connectionInfoShown = !connectionInfoShown }) {
                Box(modifier = Modifier
                    .size(24.dp)
                    .background(statusBackgroundColor, CircleShape)
                )
            }

            ConnectionInformationDialog(
                isShown = connectionInfoShown,
                connectionStatus = connectionStatus
            ) {
                connectionInfoShown = false
            }


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
                onLockPC = { viewModel.lockPC(); onMenuDismiss() },
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

        ActionsDropdownMenu(
            actions = listOf(
                Actions.openLinkAction { showURLDialog = true },
                Actions.copyToClipboardAction { showClipboardDialog = true },
                Actions.lockPCAction(onLockPC),
                Actions.shutdownPCAction(onShutdownPC)
            ),
            show = showMenu,
            onDismissRequest = onMenuDismiss
        )

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
fun ConnectionInformationDialog(
    isShown: Boolean,
    connectionStatus: ConnectionStatus,
    onDismiss: () -> Unit
) {


    DropdownMenu(modifier = Modifier.fillMaxWidth(0.7f).padding(16.dp), expanded = isShown, onDismissRequest =  onDismiss) {

        when (connectionStatus) {
            is ConnectionStatus.Connected -> {
                Text(text = "IP: ${connectionStatus.connection.ip}")
                Text(text = "Port: ${connectionStatus.connection.port}")

                if (connectionStatus.connection.ip.isGlobalIp()) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = "You are connected through the global network. Charges may apply by your ISP")
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(text = "You are connected through a local network.")
                }
            }
            else -> Text(text = "You are not connected to the computer")
        }
    }

}
