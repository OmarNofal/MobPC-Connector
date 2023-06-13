package com.omar.pcconnector.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omar.pcconnector.di.ViewModelFactoryProvider
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.ui.main.FileSystemViewModel
import com.omar.pcconnector.ui.main.ToolbarViewModel
import dagger.hilt.android.EntryPointAccessors


@Composable
fun toolbarViewModel(connection: Connection): ToolbarViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).toolbarViewModelFactory()

    return viewModel(factory = ToolbarViewModel.provideFactory(factory, connection))
}


@Composable
fun fileSystemViewModel(connection: Connection): FileSystemViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).fileSystemViewModelFactory()

    return viewModel(factory = FileSystemViewModel.provideFactory(factory, connection))
}