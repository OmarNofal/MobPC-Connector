package com.omar.pcconnector.di

import com.omar.pcconnector.ui.main.FileSystemViewModel
import com.omar.pcconnector.ui.main.ServerConnectionViewModel
import com.omar.pcconnector.ui.main.ToolbarViewModel
import com.omar.pcconnector.ui.preview.ImagePreviewViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {

    fun toolbarViewModelFactory(): ToolbarViewModel.Factory
    fun fileSystemViewModelFactory(): FileSystemViewModel.Factory
    fun imagePreviewViewModelFactory(): ImagePreviewViewModel.Factory
    fun serverConnectionViewModel(): ServerConnectionViewModel.Factory
}