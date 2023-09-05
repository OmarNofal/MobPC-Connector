package com.omar.pcconnector.ui.session

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.LocalImageLoader
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.model.TransferState
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.network.connection.ConnectionStatus
import com.omar.pcconnector.ui.MakeDirDialog
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.event.ApplicationOperation
import com.omar.pcconnector.ui.fab.FileSystemFAB
import com.omar.pcconnector.ui.fileSystemViewModel
import com.omar.pcconnector.ui.fs.FileSystemUI
import com.omar.pcconnector.ui.fs.imageLoader
import com.omar.pcconnector.ui.main.FileSystemViewModel
import com.omar.pcconnector.ui.serverConnectionViewModel
import com.omar.pcconnector.ui.toolbar.MainToolbar
import com.omar.pcconnector.ui.toolbarViewModel
import com.omar.pcconnector.ui.transfer.TransferPopup
import com.omar.pcconnector.ui.transfer.TransferViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest


val LocalConnectionProvider =
    compositionLocalOf<Connection>(defaultFactory = { throw IllegalArgumentException() })

/**
 * Corresponds to the whole UI which encompasses all states and actions
 * of a particular server
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ServerSession(
    modifier: Modifier,
    pairedDevice: PairedDevice,
    eventsFlow: SharedFlow<ApplicationEvent>
) {

    val serverConnectionViewModel = serverConnectionViewModel(pairedDevice = pairedDevice)

    val toolbarViewModel =
        toolbarViewModel(serverConnectionViewModel.connectionStatus, pairedDevice.deviceInfo.name)
    val fileSystemViewModel = fileSystemViewModel(
        serverConnectionViewModel.connectionStatus,
        pairedDevice.deviceInfo.id,
        pairedDevice.token
    )
    val transfersViewModel = hiltViewModel<TransferViewModel>()


    var transfersShown by remember {
        mutableStateOf(false)
    }
    val transfers by transfersViewModel.getTransfersFlow().collectAsState(initial = listOf())

    val snackBarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val uploadFolderIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = {
            if (it == null) return@rememberLauncherForActivityResult
            else fileSystemViewModel.upload(listOf(DocumentFile.fromTreeUri(context, it)!!))
        })

    val uploadFileIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { it ->
            if (it.isEmpty()) return@rememberLauncherForActivityResult
            fileSystemViewModel.upload(it.map { DocumentFile.fromSingleUri(context, it)!! })
        }
    )



    SnackBarEventNotifier(eventsFlow = eventsFlow, snackBarHostState = snackBarHostState)

    var showMakeDirDialog by remember { mutableStateOf(false) }

    if (showMakeDirDialog)
        MakeDirDialog(onConfirm = fileSystemViewModel::mkdirs) { showMakeDirDialog = false }

    val connectionStatus by serverConnectionViewModel.connectionStatus.collectAsState()


    val fileUiListState = rememberLazyListState()
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) {
                val visuals = it.visuals as ApplicationSnackBarVisuals
                Snackbar(
                    containerColor = visuals.backgroundColor,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = visuals.icon, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = visuals.message, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        floatingActionButton = {
            val copiedFile by fileSystemViewModel.copiedResource.collectAsState(initial = null)
            val pasteCallback = if (copiedFile == null) null else fileSystemViewModel::pasteResource
            AnimatedVisibility(
                visible = !transfersShown and fileUiListState.isScrollingUp() and (connectionStatus is ConnectionStatus.Connected),
                enter = scaleIn(tween(100)),
                exit = scaleOut(tween(100))
            ) {
                FileSystemFAB(
                    onMakeDir = { showMakeDirDialog = true },
                    onUploadFolder = { uploadFolderIntent.launch(null) },
                    onUploadFile = { uploadFileIntent.launch(arrayOf("*/*")) },
                    onPaste = pasteCallback
                )
            }
        },
        topBar = {
            MainToolbar(
                onShowTransfers = { transfersShown = !transfersShown },
                isTransferOngoing = transfers.any {
                    (it.transferState is TransferState.Running) or (it.transferState is TransferState.Initializing)
                },
                viewModel = toolbarViewModel
            )
        }
    ) { innerPadding ->


        when (val s = connectionStatus) {
            is ConnectionStatus.Searching -> SearchingForServer(Modifier.fillMaxSize())
            is ConnectionStatus.NotFound -> ConnectionNotFound(
                Modifier.fillMaxSize(),
                serverConnectionViewModel::searchAndConnect
            )

            is ConnectionStatus.Connected ->
                ConnectedScreen(
                    Modifier.fillMaxSize(),
                    pairedDevice,
                    s.connection,
                    PaddingValues(top = innerPadding.calculateTopPadding()),
                    fileSystemViewModel,
                    transfersViewModel,
                    fileUiListState,
                    transfersShown
                ) { transfersShown = false }
        }

    }


}



@Composable
fun SearchingForServer(
    modifier: Modifier
) {
    Box(modifier = modifier) {

        AlertDialog(
            onDismissRequest = { /*TODO*/ },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Searching for your computer")
                }
            },
            confirmButton = {}
        )

    }
}


@Composable
fun ConnectionNotFound(
    modifier: Modifier,
    onRetry: () -> Unit
) {
    Box(modifier = modifier) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(56.dp),
                imageVector = Icons.Rounded.SignalWifiConnectedNoInternet4,
                contentDescription = null
            )
            Text(text = "Could not find your computer", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text(text = "Try Again")
            }
        }
    }
}

@Composable
fun ConnectedScreen(
    modifier: Modifier,
    pairedDevice: PairedDevice,
    connection: Connection,
    paddingValues: PaddingValues,
    fileSystemViewModel: FileSystemViewModel,
    transfersViewModel: TransferViewModel,
    listState: LazyListState,
    transfersShown: Boolean,
    onHideTransfers: () -> Unit
) {

    CompositionLocalProvider(
        LocalConnectionProvider provides connection,
        LocalImageLoader provides imageLoader(LocalContext.current, pairedDevice.token)
    ) {

        Box(modifier = modifier) {
            FileSystemUI(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                viewModel = fileSystemViewModel,
                listState = listState
            )

            TransferPopup(
                modifier =
                Modifier
                    .fillMaxWidth(0.95f)
                    .padding(paddingValues),
                visible = transfersShown, onHideTransfers,
                onCancel = transfersViewModel::cancelTransfer,
                onDelete = transfersViewModel::deleteTransfer,
                transferViewModel = transfersViewModel
            )
        }
    }


}

@Composable
fun SnackBarEventNotifier(
    eventsFlow: SharedFlow<ApplicationEvent>,
    snackBarHostState: SnackbarHostState
) {
    LaunchedEffect(Unit) {
        eventsFlow.collectLatest {
            val snackBarColor = if (it.isSuccess) Color(0xFF42945D) else Color(0xFFE91E63)
            snackBarHostState.showSnackbar(
                ApplicationSnackBarVisuals(
                    message = it.toMessage(),
                    duration = SnackbarDuration.Short,
                    withDismissAction = true,
                    icon = it.operation.getIcon(),
                    backgroundColor = snackBarColor
                )
            )
        }
    }
}


data class ApplicationSnackBarVisuals(
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean,
    val icon: ImageVector,
    val backgroundColor: Color
) : SnackbarVisuals


private fun ApplicationOperation.getIcon(): ImageVector {
    return when (this) {
        ApplicationOperation.SHUTDOWN_PC -> Icons.Rounded.Power
        ApplicationOperation.OPEN_URL -> Icons.Rounded.Public
        ApplicationOperation.COPY_TO_CLIPBOARD -> Icons.Rounded.ContentCopy
        ApplicationOperation.LOCK_PC -> Icons.Rounded.Lock
        ApplicationOperation.PING_SERVER -> Icons.Rounded.Cloud
    }
}

fun ApplicationEvent.toMessage(): String {
    val failMessage = "Failed to "
    val successMessage = " successfully"
    return when (this.operation to this.isSuccess) {
        ApplicationOperation.LOCK_PC to true -> "PC Locked$successMessage"
        ApplicationOperation.LOCK_PC to false -> failMessage + "lock PC"
        ApplicationOperation.COPY_TO_CLIPBOARD to true -> "Copied to clipboard$successMessage"
        ApplicationOperation.COPY_TO_CLIPBOARD to false -> failMessage + "copy to clipboard"
        ApplicationOperation.OPEN_URL to true -> "URL opened$successMessage"
        ApplicationOperation.OPEN_URL to false -> failMessage + "open URL"
        ApplicationOperation.SHUTDOWN_PC to true -> "PC Shutdown $successMessage"
        ApplicationOperation.SHUTDOWN_PC to false -> failMessage + "shutdown PC"
        ApplicationOperation.PING_SERVER to true -> "Connection established"
        ApplicationOperation.PING_SERVER to false -> "Connection to the server lost"
        else -> "Unknown error"
    }
}


@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}