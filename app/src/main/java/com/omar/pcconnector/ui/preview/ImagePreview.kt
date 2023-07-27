package com.omar.pcconnector.ui.preview

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import coil.compose.AsyncImage
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.ui.imagePreviewViewModel
import java.io.File
import java.nio.file.Path


@Composable
fun ImagePreview(
    connection: Connection,
    imagePath: Path
) {
    ImagePreviewViewModel(imagePreviewViewModel(connection, imagePath))
}

@Composable
fun ImagePreviewViewModel(
    viewModel: ImagePreviewViewModel
) {

    val state by viewModel.state.collectAsState(initial = ImagePreviewState.Downloading)


    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        viewModel.events
            .collect {
                when (it) {
                    is ImagePreviewEvents.ShareEvent -> {
                        shareImage(it.uri, context)
                    }
                    else -> {}
                }
            }
    }

    ImagePreview(
        state = state,
        onShare = viewModel::onShare,
        onRetry = viewModel::onRetry,
        onCloseScreen = viewModel::closeScreen,
        onRenderError = viewModel::onRenderError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePreview(
    state: ImagePreviewState,
    onShare: () -> Unit,
    onRetry: () -> Unit,
    onCloseScreen: () -> Unit,
    onRenderError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier,
                title = {
                    Text(text = "Image Preview")
                },
                navigationIcon = {
                    IconButton(onClick = onCloseScreen) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share Image URL"
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Download Image"
                    )
                }
            )
        }
    ) { paddingValues ->
        when (state) {
            is ImagePreviewState.Downloading -> LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            is ImagePreviewState.Error -> ErrorView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                error = state.errorType,
                onRefresh = onRetry
            )

            is ImagePreviewState.Downloaded ->
                when (state) {
                    is ImagePreviewState.Downloaded.RenderError -> ErrorView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        error = state.error,
                        onRefresh = onRetry
                    )

                    else -> ImageView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        imageUri = state.uri,
                        onError = onRenderError
                    )
                }
        }
    }
}


@Composable
private fun LoadingView(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ImageView(
    modifier: Modifier,
    imageUri: Uri,
    onError: () -> Unit,
) {
    AsyncImage(
        modifier = modifier,
        model = imageUri,
        contentDescription = "Preview",
        onError = { onError() },
        contentScale = ContentScale.Fit,
        alignment = Alignment.Center
    )
}

@Composable
private fun ErrorView(
    modifier: Modifier,
    error: ImagePreviewErrors,
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Rounded.ErrorOutline, contentDescription = "Error")
        Text(text = errorToString(error))
        Button(onClick = onRefresh) {
            Text(text = "Retry")
        }
    }
}

private fun errorToString(errors: ImagePreviewErrors) = when (errors) {
    ImagePreviewErrors.RENDER_ERROR -> "Failed to render the image"
    ImagePreviewErrors.NETWORK_EXCEPTION -> "A network error occurred"
    ImagePreviewErrors.UNKNOWN_EXCEPTION -> "An unknown error occurred"
}


private fun shareImage(uri: Uri, context: Context) {
    val file = uri.toFile()
    val shareableUri = FileProvider.getUriForFile(
        context,
        "com.omar.pcconnector.provider",
        file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, shareableUri)
    }
    val chooser = Intent.createChooser(intent, "Share Image")
    context.startActivity(chooser)
}