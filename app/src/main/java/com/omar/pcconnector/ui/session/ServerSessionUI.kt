package com.omar.pcconnector.ui.session

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.TransferState
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.ui.MakeDirDialog
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.fab.FileSystemFAB
import com.omar.pcconnector.ui.fileSystemViewModel
import com.omar.pcconnector.ui.fs.FileSystemUI
import com.omar.pcconnector.ui.toolbar.MainToolbar
import com.omar.pcconnector.ui.toolbarViewModel
import com.omar.pcconnector.ui.transfer.TransferPopup
import com.omar.pcconnector.ui.transfer.TransferViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest


/**
 * Corresponds to the whole UI which encompasses all states and actions
 * of a particular server
 */
@Composable
fun ServerSession(
    modifier: Modifier,
    connection: Connection,
    eventsFlow: SharedFlow<ApplicationEvent>
) {

    val toolbarViewModel = toolbarViewModel(connection = connection)
    val fileSystemViewModel = fileSystemViewModel(connection = connection)
    val transfersViewModel = hiltViewModel<TransferViewModel>()


    val copiedFile by fileSystemViewModel.copiedResource.collectAsState(initial = null)

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



    LaunchedEffect(Unit) {
        eventsFlow.collectLatest {
            snackBarHostState.showSnackbar(
                it.toMessage(),
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    var showMakeDirDialog by remember { mutableStateOf(false) }

    if (showMakeDirDialog)
        MakeDirDialog(onConfirm = fileSystemViewModel::mkdirs) { showMakeDirDialog = false }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        floatingActionButton = {
            val pasteCallback = if (copiedFile == null) null else fileSystemViewModel::pasteResource
            FileSystemFAB(
                onMakeDir = { showMakeDirDialog = true },
                onUploadFolder = { uploadFolderIntent.launch(null) },
                onUploadFile = { uploadFileIntent.launch(arrayOf("*/*")) },
                onPaste = pasteCallback
            )
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

        Box(modifier = Modifier.fillMaxSize()) {
            FileSystemUI(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                viewModel = fileSystemViewModel
            )

            TransferPopup(
                modifier =
                Modifier
                    .fillMaxWidth(0.9f),
                visible = transfersShown, { transfersShown = false },
                onCancel = transfersViewModel::cancelTransfer,
                onDelete = transfersViewModel::deleteTransfer,
                transferViewModel = transfersViewModel
            )
        }

    }


}