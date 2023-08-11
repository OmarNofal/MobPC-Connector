package com.omar.pcconnector.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omar.pcconnector.di.ViewModelFactoryProvider
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.network.api.Token
import com.omar.pcconnector.network.connection.ConnectionStatus
import com.omar.pcconnector.ui.main.FileSystemViewModel
import com.omar.pcconnector.ui.main.ServerConnectionViewModel
import com.omar.pcconnector.ui.main.ToolbarViewModel
import com.omar.pcconnector.ui.preview.ImagePreviewViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Retrofit
import java.nio.file.Path


@Composable
fun toolbarViewModel(connectionStatus: StateFlow<ConnectionStatus>, serverName: String): ToolbarViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).toolbarViewModelFactory()

    return viewModel(factory = ToolbarViewModel.provideFactory(factory, connectionStatus, serverName))
}


@Composable
fun fileSystemViewModel(connectionStatus: StateFlow<ConnectionStatus>, serverId: String, token: Token): FileSystemViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).fileSystemViewModelFactory()

    return viewModel(factory = FileSystemViewModel.provideFactory(factory, connectionStatus, serverId, token))
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
fun imagePreviewViewModel(retrofit: Retrofit, imagePath: Path): ImagePreviewViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).imagePreviewViewModelFactory()

    return viewModel(factory = ImagePreviewViewModel.provideFactory(factory, retrofit, imagePath))
}