package com.omar.pcconnector.ui.toolbar


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    isTransferOngoing: Boolean,
    onOpenDrawer: () -> Unit,
) {

    var menuShown by remember { mutableStateOf(false) }
    val connectionStatus by viewModel.connectionStatusFlow.collectAsState()

    TopAppBar(
        title = {
            Text(
                text = viewModel.serverName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.surfaceVariant),
        actions = {


            val statusBackgroundColor = when (val s = connectionStatus) {
                is ConnectionStatus.Connected -> if (s.connection.ip.isGlobalIp()) darkYellow else darkGreen

                else -> Color.Red
            }
            var connectionInfoShown by remember {
                mutableStateOf(false)
            }

            IconButton(onClick = {
                connectionInfoShown = !connectionInfoShown
            }) {
                BadgedBox(badge = { }) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = statusBackgroundColor
                    )
                }
            }


            ConnectionInformationDialog(
                isShown = connectionInfoShown,
                connectionStatus = connectionStatus
            ) {
                connectionInfoShown = false
            }


            IconButton(onClick = onShowTransfers) {

                val colors = listOf(
                    Color(0xFFED4264),
                    Color(0xFFFFEDBC),
                    Color(0xFFED4264)
                )

                Icon(
                    imageVector = Icons.Rounded.SwapVert,
                    contentDescription = "Actions",
                    if (isTransferOngoing) Modifier.drawAnimatedBorder(
                        2.dp, CircleShape, colors, durationMillis = 1000
                    )
                    else Modifier
                )
            }

            IconButton(onClick = { menuShown = !menuShown }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Actions"
                )
            }

            val onMenuDismiss = { menuShown = false }
            MainToolbarOverflow(
                onOpenBrowser = viewModel::openLinkInBrowser,
                onCopyClipboard = viewModel::copyToPCClipboard,
                onLockPC = { viewModel.lockPC(); onMenuDismiss() },
                onShutdownPC = { viewModel.shutdownPC(); onMenuDismiss() },
                showMenu = menuShown,
                onMenuDismiss = onMenuDismiss,
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Drawer"
                )
            }
        })


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
                Actions.copyToClipboardAction {
                    showClipboardDialog = true
                },
                Actions.lockPCAction(onLockPC),
                Actions.shutdownPCAction(onShutdownPC)
            ), show = showMenu, onDismissRequest = onMenuDismiss
        )

        if (showURLDialog) {
            URLDialog(onDismiss = { showURLDialog = false },
                      onConfirm = { url, incognito ->
                          onOpenBrowser(url, incognito)
                          onMenuDismiss()
                      })
        } else if (showClipboardDialog) {
            ClipboardDialog(onDismiss = { showClipboardDialog = false },
                            onConfirm = { text -> onCopyClipboard(text); onMenuDismiss() })
        }


    }
}


@Composable
fun ConnectionInformationDialog(
    isShown: Boolean, connectionStatus: ConnectionStatus, onDismiss: () -> Unit
) {


    DropdownMenu(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .padding(16.dp),
        expanded = isShown,
        onDismissRequest = onDismiss
    ) {

        when (connectionStatus) {
            is ConnectionStatus.Connected -> {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "IP")
                    Text(
                        text = connectionStatus.connection.ip,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Port")
                    Text(
                        text = connectionStatus.connection.port.toString(),
                        fontWeight = FontWeight.Medium
                    )
                }

                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                )

                if (connectionStatus.connection.ip.isGlobalIp()) {
                    Spacer(Modifier.height(8.dp))
                    Column {
                        Text(
                            text = "You are connected through the global network\n",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Charges may apply by your ISP",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "You are connected through a local network",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            else -> Text(text = "You are not connected to the computer")
        }
    }

}
