package com.omar.pcconnector.ui.fs


import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.bytesToSizeString
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.ui.DeleteDialog
import com.omar.pcconnector.ui.RenameDialog
import com.omar.pcconnector.ui.action.Actions
import com.omar.pcconnector.ui.action.ActionsDropdownMenu
import com.omar.pcconnector.ui.main.FileSystemState
import com.omar.pcconnector.ui.main.FileSystemViewModel


/**
 * This is the view which allows the
 * user to interact with the file system of the PC
 */
@Composable
fun FileSystemUI(
    modifier: Modifier,
    viewModel: FileSystemViewModel,
    nestedScroll: NestedScrollConnection
) {

    BackHandler(true) {
        viewModel.onNavigateBack()
    }

    val state by viewModel.state.collectAsState(FileSystemState.Loading)

    when (state) {
        is FileSystemState.Loading -> LoadingScreen(modifier)
        is FileSystemState.Initialized.Loading -> FileSystemTree(
            modifier = modifier,
            nestedScroll,
            (state as FileSystemState.Initialized.Loading).currentDirectory.absolutePath,
            listOf(),
            true,
            viewModel::onResourceClicked,
            viewModel::renameResource,
            viewModel::deleteResource,
            viewModel::download,
            viewModel::copyResource
        )
        is FileSystemState.Initialized.Loaded -> FileSystemTree(
            modifier = modifier,
            nestedScroll,
            (state as FileSystemState.Initialized.Loaded).currentDirectory.absolutePath,
            (state as FileSystemState.Initialized.Loaded).directoryStructure,
            false,
            viewModel::onResourceClicked,
            viewModel::renameResource,
            viewModel::deleteResource,
            viewModel::download,
            viewModel::copyResource
        )
    }

}


@Composable
fun LoadingScreen(
    modifier: Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun FileSystemTree(
    modifier: Modifier,
    nestedScroll: NestedScrollConnection,
    currentDirectory: String = "~",
    directoryStructure: List<Resource> = listOf(),
    isLoading: Boolean = false,
    onResourceClicked: (Resource) -> Unit,
    onRename: (Resource, String, Boolean) -> Unit,
    onDelete: (Resource) -> Unit,
    onResourceDownload: (Resource, DocumentFile) -> Unit,
    onResourceCopied: (Resource) -> Unit
) {

    Box(modifier = modifier) {
        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }
        LazyColumn(Modifier.nestedScroll(nestedScroll)) {

            item {
                Text(
                    text = "You are in $currentDirectory",
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(directoryStructure, key = { it.name + it.creationDateMs }) {
                ResourceRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(),
                    resource = it,
                    onClick = { onResourceClicked(it) },
                    onRename = { newName, overwrite -> onRename(it, newName, overwrite) },
                    onDelete = { onDelete(it) },
                    onDownload = { file -> onResourceDownload(it, file) },
                    onCopied = { onResourceCopied(it) }
                )

                if (it != directoryStructure.last()) {
                    Divider(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }
            }
        }

    }

}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResourceRow(
    modifier: Modifier,
    resource: Resource,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    onRename: (String, Boolean) -> Unit = { _, _ -> },
    onDelete: () -> Unit = {},
    onDownload: (DocumentFile) -> Unit,
    onCopied: () -> Unit
) {

    val context = LocalContext.current

    val downloadDirPickerContract = directoryPicker {
        Log.i("PICKED DIRECTORY", it.toString())
        if (it == null) return@directoryPicker
        else onDownload(DocumentFile.fromTreeUri(context, it)!!)
    }

    var showRenameDialog by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }


    Row(
        modifier
            .combinedClickable(
                onLongClick = {
                    onLongPress()
                    Toast
                        .makeText(context, "Long pressed", Toast.LENGTH_LONG)
                        .show()
                }
            ) {
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon =
            if (resource is DirectoryResource) Icons.Rounded.Folder else Icons.Rounded.Description
        Icon(
            imageVector = icon,
            contentDescription = "Directory icon",
            modifier = Modifier.size(34.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = resource.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            val subText =
                if (resource is DirectoryResource) "${resource.numResources} items" else resource.size.bytesToSizeString()
            Text(text = subText, fontWeight = FontWeight.Light, fontSize = 12.sp)
        }

        Column {

            var isMenuOpen by remember { mutableStateOf(false) }
            IconButton(
                onClick = { isMenuOpen = !isMenuOpen },
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "More Options")
            }
            ActionsDropdownMenu(
                actions = listOf(
                    Actions.downloadAction { downloadDirPickerContract.launch(null) },
                    Actions.copyAction(onCopied),
                    Actions.renameAction { showRenameDialog = true },
                    Actions.deleteAction { showDeleteDialog = true },
                ),
                show = isMenuOpen,
                onDismissRequest = { isMenuOpen = false }
            )
        }
    }

    if (showRenameDialog)
        RenameDialog(
            oldName = resource.name,
            onConfirm = { newName, overwrite -> onRename(newName, overwrite) },
            onCancel = { showRenameDialog = false }
        )

    if (showDeleteDialog)
        DeleteDialog(
            fileName = resource.name,
            onConfirm = onDelete,
            onCancel = { showDeleteDialog = false }
        )

}


@Composable
fun directoryPicker(
    onDirectoryPicked: (Uri?) -> Unit
): ManagedActivityResultLauncher<Uri?, Uri?> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = onDirectoryPicked
    )
}
