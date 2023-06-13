package com.omar.pcconnector.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.TransferState
import com.omar.pcconnector.ui.fs.FileSystemUI
import com.omar.pcconnector.ui.toolbar.MainToolbar
import com.omar.pcconnector.ui.transfer.TransferPopup
import com.omar.pcconnector.ui.transfer.TransferViewModel


@Composable
fun MainApp() {

    var transfersShown by remember {
        mutableStateOf(false)
    }

    val transfersViewModel = hiltViewModel<TransferViewModel>()
    val transfers by transfersViewModel.getTransfersFlow().collectAsState(initial = listOf())

    Box(contentAlignment = Alignment.TopCenter) {


        FileSystemUI(
            Modifier
                .padding(top = 56.dp)
                .fillMaxSize()
                , hiltViewModel()
        )


        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainToolbar(
                onShowTransfers = { transfersShown = !transfersShown },
                isTransferOngoing = transfers.any {
                    (it.transferState is TransferState.Running) or (it.transferState is TransferState.Initializing)
                }
            )
            //Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                TransferPopup(
                    modifier =
                    Modifier
                        .fillMaxWidth(0.9f),
                    visible = transfersShown, { transfersShown = false },
                    onCancel = transfersViewModel::cancelTransfer,
                    onDelete = transfersViewModel::deleteTransfer,
                    transferViewModel = transfersViewModel
                )
            }

        }
    }

}