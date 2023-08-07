package com.omar.pcconnector.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omar.pcconnector.di.ViewModelFactoryProvider
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.network.connection.ServerConnection
import com.omar.pcconnector.ui.main.FileSystemViewModel
import com.omar.pcconnector.ui.main.ServerConnectionViewModel
import com.omar.pcconnector.ui.main.ToolbarViewModel
import com.omar.pcconnector.ui.preview.ImagePreviewViewModel
import dagger.hilt.android.EntryPointAccessors
import java.nio.file.Path


@Composable
fun toolbarViewModel(serverConnection: ServerConnection, serverName: String): ToolbarViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).toolbarViewModelFactory()

    return viewModel(factory = ToolbarViewModel.provideFactory(factory, serverConnection, serverName))
}


@Composable
fun fileSystemViewModel(serverConnection: ServerConnection): FileSystemViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).fileSystemViewModelFactory()

    return viewModel(factory = FileSystemViewModel.provideFactory(factory, serverConnection))
}

@Composable
fun serverConnectionViewModel(pairedDevice: PairedDevice): ServerConnectionViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).serverConnectionViewModel()

    return viewModel(factory = ServerConnectionViewModel.provideFactory(factory, pairedDevice))
}

@Composable
fun imagePreviewViewModel(connection: Connection, imagePath: Path): ImagePreviewViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).imagePreviewViewModelFactory()

    return viewModel(factory = ImagePreviewViewModel.provideFactory(factory, connection, imagePath))
}