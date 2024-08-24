package com.omar.pcconnector.ui.fs


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.isSupportedImageExtension
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.ui.main.FileSystemState
import com.omar.pcconnector.ui.main.FileSystemViewModel
import com.omar.pcconnector.ui.main.INVALID_PATH
import com.omar.pcconnector.ui.session.LocalImageCallbacks
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

    val loadedState = state as FileSystemState.Loaded
    val directoryStructure = loadedState.directoryStructure
    val isLoadingADirectory = loadedState.currentlyLoadingDirectory != null

    FileSystemTree(
        modifier = modifier,
        listState = listState,
        currentDirectory = loadedState.currentDirectory.absolutePath,
        directoryStructure = directoryStructure,
        drives = loadedState.drives,
        isLoading = isLoadingADirectory,
        onResourceClicked = viewModel::onResourceClicked,
        onPathChanged = viewModel::onPathChanged,
        onRename = viewModel::renameResource,
        onDelete = viewModel::deleteResource,
        onResourceDownload = viewModel::download,
        onResourceCopied = viewModel::copyResource
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun FileSystemTree(
    modifier: Modifier,
    listState: LazyListState,
    currentDirectory: String,
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


            var isSearchFilterEnabled by remember(currentDirectory) {
                mutableStateOf(false)
            }
            var searchFilter by remember(currentDirectory) { mutableStateOf("") }

            LocationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                path = currentDirectory,
                drives = drives,
                onPathSelected = onPathChanged,
                toggleSearchFilter = {
                    isSearchFilterEnabled = !isSearchFilterEnabled
                },
                isSearchFilterEnabled,
                searchFilter,
                { searchFilter = it }
            )
            HorizontalDivider(Modifier.fillMaxWidth())


            val fsState =
                remember(currentDirectory, directoryStructure, isLoading) {
                    FileSystemTreeState(
                        currentDirectory,
                        directoryStructure,
                        isLoading
                    )
                }
            AnimatedContent(
                targetState = fsState, label = "",
                //transitionSpec = { createTransition() }
            ) { fsState ->

                val directory = fsState.content

                val directoryItems = remember(
                    key1 = searchFilter,
                    key2 = isSearchFilterEnabled
                ) {
                    if (isSearchFilterEnabled) directory.filter {
                        it.name.contains(
                            searchFilter,
                            true
                        )
                    }
                    else directory
                }

                val imageClickedCallback = LocalImageCallbacks.current
                if (fsState.content.isEmpty() && fsState.directory != INVALID_PATH.absolutePath)
                    EmptyDirectoryMessage(modifier = Modifier.fillMaxSize())
                else
                    LazyColumn(Modifier.fillMaxSize(), state = listState) {

                        items(
                            directoryItems,
                            key = { it.name + it.creationDateMs }) {


                            ResourceRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement(),
                                resource = it,
                                onClick = {
                                    if (it.path.extension.isSupportedImageExtension()) {
                                        imageClickedCallback.invoke(it.path.toString())
                                    } else
                                        onResourceClicked(it)

                                },
                                onRename = { newName, overwrite ->
                                    onRename(
                                        it,
                                        newName,
                                        overwrite
                                    )
                                },
                                onDelete = { onDelete(it) },
                                onDownload = { file ->
                                    onResourceDownload(
                                        it,
                                        file
                                    )
                                },
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

        // Only show loading bar after some time of waiting
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
