package com.omar.pcconnector.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.ui.fs.FileSystemUI
import com.omar.pcconnector.ui.toolbar.MainToolbar


@Composable
fun MainApp() {

    Column {
        MainToolbar()
        FileSystemUI(Modifier.fillMaxSize(), hiltViewModel())
    }

}