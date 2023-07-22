package com.omar.pcconnector.ui.fs


import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.omar.pcconnector.R
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
import com.omar.pcconnector.ui.session.LocalConnectionProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.nio.file.Path
import kotlin.io.path.extension


/**
 * This is the view which allows the
 * user to interact with the file system of the PC
 */
@Composable
fun FileSystemUI(
    modifier: Modifier,
    viewModel: FileSystemViewModel,
    listState: LazyListState
) {

    BackHandler(true) {
        viewModel.onNavigateBack()
    }

    val state by viewModel.state.collectAsState(FileSystemState.Loading)




    if (state is FileSystemState.Loading) {
        LoadingScreen(modifier)
        return
    }

    val isLoading = state is FileSystemState.Initialized.Loading
    val directoryStructure =
        if (state is FileSystemState.Initialized) (state as FileSystemState.Initialized).directoryStructure
        else emptyList()

    FileSystemTree(
        modifier = modifier,
        listState,
        (state as FileSystemState.Initialized).currentDirectory.absolutePath,
        directoryStructure,
        (state as FileSystemState.Initialized).drives,
        isLoading,
        viewModel::onResourceClicked,
        viewModel::onPathChanged,
        viewModel::renameResource,
        viewModel::deleteResource,
        viewModel::download,
        viewModel::copyResource
    )

}


@Composable
fun LoadingScreen(
    modifier: Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun FileSystemTree(
    modifier: Modifier,
    listState: LazyListState,
    currentDirectory: String = "~",
    directoryStructure: List<Resource> = listOf(),
    drives: List<String>,
    isLoading: Boolean = false,
    onResourceClicked: (Resource) -> Unit,
    onPathChanged: (Path) -> Unit,
    onRename: (Resource, String, Boolean) -> Unit,
    onDelete: (Resource) -> Unit,
    onResourceDownload: (Resource, DocumentFile) -> Unit,
    onResourceCopied: (Resource) -> Unit
) {

    Box(modifier = modifier) {


        Column {


            var isSearchFilterEnabled by remember(currentDirectory) { mutableStateOf(false) }
            var searchFilter by remember(currentDirectory) { mutableStateOf("") }

            LocationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                path = currentDirectory,
                drives = drives,
                onPathSelected = onPathChanged,
                toggleSearchFilter = { isSearchFilterEnabled = !isSearchFilterEnabled },
                isSearchFilterEnabled,
                searchFilter,
                { searchFilter = it }
            )
            Divider(Modifier.fillMaxWidth())

            AnimatedContent(
                targetState = directoryStructure
            ) { directory ->


                val directoryItems = remember(key1 = searchFilter, key2 = isSearchFilterEnabled) {
                    if (isSearchFilterEnabled) directory.filter {
                        it.name.contains(
                            searchFilter,
                            true
                        )
                    }
                    else directory
                }

                if (directoryItems.isEmpty())
                    EmptyDirectoryMessage(modifier = Modifier.fillMaxSize())
                else
                    LazyColumn(Modifier.fillMaxSize(), state = listState) {

                        items(directoryItems, key = { it.name + it.creationDateMs }) {
                            ResourceRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement(),
                                resource = it,
                                onClick = { onResourceClicked(it) },
                                onRename = { newName, overwrite ->
                                    onRename(
                                        it,
                                        newName,
                                        overwrite
                                    )
                                },
                                onDelete = { onDelete(it) },
                                onDownload = { file -> onResourceDownload(it, file) },
                                onCopied = { onResourceCopied(it) }
                            )

                            if (it != directory.last()) {
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


        var showLoadingBar by remember {
            mutableStateOf(false)
        }

        if (showLoadingBar) {
            LinearProgressIndicator(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        // Only show loading bar after 1 second of waiting
        LaunchedEffect(key1 = isLoading) {
            if (!isLoading) {
                showLoadingBar = false
                return@LaunchedEffect
            }
            delay(400L)
            if (isActive)
                showLoadingBar = true
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

        ResourceIcon(
            Modifier.size(34.dp),
            resource
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
fun ResourceIcon(
    modifier: Modifier,
    resource: Resource
) {

    if (resource is DirectoryResource) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = "Directory icon",
            modifier = modifier
        )
    } else if (resource.path.extension.lowercase() == "pdf") {
        Icon(
            painter = painterResource(R.drawable.pdf),
            modifier = modifier,
            contentDescription = null
        )
    } else if (resource.path.extension.endsWith("jpg", true)) {
        ImagePreviewIcon(modifier = modifier.clip(RoundedCornerShape(4.dp)), resource = resource)
    } else if (resource.path.extension.endsWith("mp4", true)) {
        VideoPreviewIcon(modifier = modifier.clip(RoundedCornerShape(4.dp)), resource = resource)
    }


}

@Composable
fun ImagePreviewIcon(
    modifier: Modifier,
    resource: Resource
) {
    val connection = LocalConnectionProvider.current
    val resourceURL = "http://${connection.ip}:${connection.port}/downloadFiles?src=${resource.path}"
    Log.i("RESOURCE URL IMAGE", resourceURL)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(resourceURL)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.pdf),
        error = painterResource(R.drawable.pdf),
        fallback = painterResource(R.drawable.pdf),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,

    )
}


@Composable
fun VideoPreviewIcon(
    modifier: Modifier,
    resource: Resource
) {
    val connection = LocalConnectionProvider.current
    val resourceURL = "http://${connection.ip}:${connection.port}/downloadFiles?src=${resource.path}"
    Log.i("URL", resourceURL)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(resourceURL)
            .decoderFactory(VideoFrameDecoder.Factory())
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.pdf),
        error = painterResource(R.drawable.pdf),
        fallback = painterResource(R.drawable.pdf),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        onError = { it.result.throwable.toString().also { Log.e("ERR VIDEO", it) }},
        onLoading = { Log.i("VIDEO", it.toString())},
        onSuccess = {Log.i("VIDEO SUCCESS", it.toString())}
        )
}

@Composable
fun EmptyDirectoryMessage(modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "¯\\_(ツ)_/¯", fontSize = 36.sp)
        Spacer(Modifier.height(8.dp))
        Text(text = "Nothing here!")
    }
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
