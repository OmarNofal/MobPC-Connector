package com.omar.pcconnector.ui.fs


import android.annotation.SuppressLint
import android.content.ContentResolver
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.ui.fab.FabItem
import com.omar.pcconnector.ui.fab.MultiItemFab
import com.omar.pcconnector.ui.main.FileSystemState
import com.omar.pcconnector.ui.main.FileSystemViewModel
import kotlin.io.path.absolutePathString
import kotlin.math.pow


/**
 * This is the view which allows the
 * user to interact with the file system of the PC
 */
@Composable
fun FileSystemUI(
    modifier: Modifier,
    viewModel: FileSystemViewModel = hiltViewModel()
) {

    BackHandler(true) {
        viewModel.onNavigateBack()
    }

    val state by viewModel.state.collectAsState(FileSystemState.Loading())

    when (state) {
        is FileSystemState.Loading -> LoadingScreen(modifier)
        else -> FileSystemTree(
            modifier = modifier,
            state.currentDirectory.absolutePathString(),
            state.directoryStructure,
            state.isLoading,
            viewModel::onResourceClicked,
            viewModel::renameResource,
            viewModel::deleteResource,
            viewModel::mkdirs,
            viewModel::download,
            viewModel::upload
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


@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun FileSystemTree(
    modifier: Modifier,
    currentDirectory: String = "~",
    directoryStructure: List<Resource> = listOf(),
    isLoading: Boolean = false,
    onResourceClicked: (Resource) -> Unit,
    onRename: (Resource, String, Boolean) -> Unit,
    onDelete: (Resource) -> Unit,
    onMakeDir: (String) -> Unit,
    onResourceDownload: (Resource, DocumentFile, ContentResolver) -> Unit,
    onUpload: (List<DocumentFile>, ContentResolver) -> Unit
) {
    val context = LocalContext.current
    var showMkdirDialog by remember {
        mutableStateOf(false)
    }

    val uploadFolderIntent = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(), onResult = {
        if (it == null) return@rememberLauncherForActivityResult
        else onUpload(listOf(DocumentFile.fromTreeUri(context, it)!!), context.contentResolver)
    })

    val uploadFileIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { it ->
            if (it.isEmpty()) return@rememberLauncherForActivityResult
            onUpload( it.map { DocumentFile.fromSingleUri(context, it)!! }, context.contentResolver)
        }
    )

    Scaffold(
        floatingActionButton = {
            var isShown by remember { mutableStateOf(false) }
            MultiItemFab(
                icon = Icons.Rounded.Add,
                onClicked = { isShown = !isShown },
                expanded = isShown,
                items = listOf(
                    FabItem("Create a Folder", Icons.Rounded.CreateNewFolder) {
                        isShown = false
                        showMkdirDialog = true },
                    FabItem("Upload a Folder", Icons.Rounded.DriveFolderUpload) {
                        isShown = false
                        uploadFolderIntent.launch(
                        null
                    ) },
                    FabItem("Upload a File", Icons.Rounded.UploadFile) {
                        isShown = false
                        uploadFileIntent.launch(arrayOf("*/*")) },
                )
            ) {
                isShown = false
            }
        }
    ) { _ ->

        LazyColumn(modifier) {
            if (isLoading) {
                item {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }

            item {
                Text(
                    text = "You are in $currentDirectory",
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(directoryStructure, key = { it.name + it.creationDateMs }) {
                ResourceRow(
                    modifier = Modifier.fillMaxWidth(),
                    resource = it,
                    onClick = { onResourceClicked(it) },
                    onRename = { newName, overwrite -> onRename(it, newName, overwrite) },
                    onDelete = { onDelete(it) },
                    onDownload = { file -> onResourceDownload(it, file, context.contentResolver) }
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


    if (showMkdirDialog) {
        MakeDirDialog(
            onConfirm = onMakeDir,
            onCancel = { showMkdirDialog = false }
        )
    }


}

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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResourceRow(
    modifier: Modifier,
    resource: Resource,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    onRename: (String, Boolean) -> Unit = { _, _ -> },
    onDelete: () -> Unit = {},
    onDownload: (DocumentFile) -> Unit
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
            IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "More Options")
            }
            ResourceActionMenu(
                actions = listOf(
                    Actions.downloadAction { downloadDirPickerContract.launch(null) },
                    Actions.copyAction(onClick),
                    Actions.renameAction { showRenameDialog = true },
                    Actions.deleteAction { showDeleteDialog = true },
                ),
                show = isMenuOpen,
                onDismissRequest = { isMenuOpen = false })
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


val sizeRanges = arrayOf(
    2.0.pow(10.0).toLong() until 2.0.pow(20.0).toLong() to "KB",
    2.0.pow(20.0).toLong() until 2.0.pow(30.0).toLong() to "MB",
    2.0.pow(30.0).toLong() until Long.MAX_VALUE to "GB"
)

// Converts size in bytes to human-readable format
// ex 4096bytes = 4KB
fun Long.bytesToSizeString(): String {

    if (this in 0 until 1024) return "$this Bytes"

    val result = try {
        val sizeRange = sizeRanges.first { this in it.first }
        "${this / sizeRange.first.first} ${sizeRange.second}"
    } catch (e: NoSuchElementException) {
        "Unknown size"
    }

    return result
}


data class ResourceAction(
    val actionName: String,
    val actionIcon: ImageVector?,
    val onClick: () -> Unit
)

object Actions {
    fun deleteAction(onClick: () -> Unit) = ResourceAction("Delete", Icons.Rounded.Delete, onClick)
    fun renameAction(onClick: () -> Unit) = ResourceAction("Rename", Icons.Rounded.Edit, onClick)
    fun copyAction(onClick: () -> Unit) = ResourceAction("Copy", Icons.Rounded.ContentCopy, onClick)
    fun downloadAction(onClick: () -> Unit) =
        ResourceAction("Download", Icons.Rounded.Download, onClick)
}


@Composable
fun ResourceActionMenu(
    modifier: Modifier = Modifier,
    actions: List<ResourceAction>,
    show: Boolean,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(modifier = modifier, expanded = show, onDismissRequest = onDismissRequest) {
        actions.forEach {

            DropdownMenuItem(
                text = {
                    Row(
                        Modifier.padding(top = 4.dp, bottom = 4.dp, end = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        it.actionIcon?.let { icon ->
                            Icon(imageVector = icon, contentDescription = "")
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = it.actionName)
                    }

                }, onClick = it.onClick
            )

            if (actions.indexOf(it) != actions.lastIndex) Divider(modifier = Modifier.padding(start = 16.dp))

        }
    }
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
fun directoryPicker(
    onDirectoryPicked: (Uri?) -> Unit
): ManagedActivityResultLauncher<Uri?, Uri?> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = onDirectoryPicked
    )
}
