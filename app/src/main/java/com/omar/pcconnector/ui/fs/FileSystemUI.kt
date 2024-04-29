package com.omar.pcconnector.ui.fs


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader
import coil.request.ImageRequest
import com.omar.pcconnector.R
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.bytesToSizeString
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.secureClient
import com.omar.pcconnector.network.connection.TokenInterceptor
import com.omar.pcconnector.supportedImageExtension
import com.omar.pcconnector.ui.DeleteDialog
import com.omar.pcconnector.ui.RenameDialog
import com.omar.pcconnector.ui.action.Actions
import com.omar.pcconnector.ui.action.ActionsDropdownMenu
import com.omar.pcconnector.ui.main.FileSystemState
import com.omar.pcconnector.ui.main.FileSystemViewModel
import com.omar.pcconnector.ui.session.LocalConnectionProvider
import com.omar.pcconnector.ui.theme.iconForExtension
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        viewModel.openFileEvents.collect {
            openFile(context, it.first, it.second)
        }
    }


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


data class FileSystemTreeState(
    val directory: String,
    val content: List<Resource>,
    val isLoading: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if (other !is FileSystemTreeState) return false

        return content == other.content
    }

    override fun hashCode(): Int {
        var result = directory.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + isLoading.hashCode()
        return result
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


            val fsState = remember(currentDirectory, directoryStructure, isLoading) {
                FileSystemTreeState(
                    currentDirectory,
                    directoryStructure,
                    isLoading
                )
            }
            AnimatedContent(
                targetState = fsState, label = "",
                transitionSpec = { createTransition() }
            ) { fsState ->

                val directory = fsState.content

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
                                        .padding(start = 82.dp)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.navigationBarsPadding())
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
            .padding(end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ResourceIcon(
            Modifier
                .padding(horizontal = 28.dp)
                .size(26.dp),
            resource
        )

        //Spacer(modifier = Modifier.width(32.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = resource.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))

            val dateFormat = remember {
                DateFormat.getDateInstance()
            }

            val subText =
                if (resource is DirectoryResource) "${resource.numResources} items" else resource.size.bytesToSizeString() + ",  ${
                    dateFormat.format(
                        resource.creationDateMs
                    )
                }"
            Text(
                text = subText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurface
        )
    } else if (resource.path.extension.lowercase() in supportedImageExtension) {
        ImagePreviewIcon(modifier = modifier.clip(RoundedCornerShape(4.dp)), resource = resource)
    } else {
        Icon(
            painter = painterResource(id = iconForExtension(resource.path.extension)),
            contentDescription = "Directory icon",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}


fun imageLoader(context: Context, token: String): ImageLoader {
    val client = secureClient.addInterceptor(TokenInterceptor(token))
        .build()
    return ImageLoader.Builder(context)
        .okHttpClient(client)
        .crossfade(true)
        .build()
}

@Composable
fun ImagePreviewIcon(
    modifier: Modifier,
    resource: Resource
) {
    val connection = LocalConnectionProvider.current
    val resourceURL =
        "https://${connection.ip}:${connection.port}/downloadFiles?src=${resource.path}"
    val loader = LocalImageLoader.current
    Log.i("RESOURCE URL IMAGE", resourceURL)

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(resourceURL)
            .crossfade(true)
            .build(),

        placeholder = painterResource(R.drawable.image),
        error = painterResource(R.drawable.image),
        fallback = painterResource(R.drawable.image),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        imageLoader = loader
    )
}


private fun openFile(context: Context, url: String, name: String) {
    val mimeType = Files.probeContentType(Paths.get(name))
    val intent = Intent(Intent.ACTION_VIEW).let {
        it.setDataAndType(Uri.parse(url), mimeType)
        Intent.createChooser(it, "View $name")
    }
    context.startActivity(intent)
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
