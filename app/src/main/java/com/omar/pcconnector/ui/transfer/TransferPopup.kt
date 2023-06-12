package com.omar.pcconnector.ui.transfer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.TransferOperation
import com.omar.pcconnector.model.TransferState
import com.omar.pcconnector.model.TransferType


@Composable
fun TransferPopup(
    modifier: Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    onCancel: (String) -> Unit,
    transferViewModel: TransferViewModel = hiltViewModel(),
) {

    val transfers by transferViewModel.getTransfersFlow().collectAsState(initial = listOf())

    val boxModifier = if (visible) Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures {
                onDismiss()
            }
        }
    else Modifier.fillMaxSize()

    BackHandler(visible, onDismiss)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = boxModifier,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(expandFrom = Alignment.Top) { 0 } + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) { 0 } + fadeOut()
        )
        {

            Card(
                modifier.padding(start = 6.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
            ) {
                if (transfers.isEmpty()) {
                    Text(
                        text = "No Running Transfers",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        fontStyle = FontStyle.Italic
                    )
                    return@Card
                }

                val downloads = transfers.filter { op -> op.transferType == TransferType.DOWNLOAD }
                val uploads = transfers.filter { op -> op.transferType == TransferType.UPLOAD }

                LazyColumn {

                    if (downloads.isNotEmpty()) {
                        item {
                            Text(
                                text = "Downloads",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                                fontSize = 14.sp
                            )
                        }
                        items(downloads) {
                            TransferRow(Modifier.fillMaxWidth(), it) { onCancel(it.id) }
                        }
                    }

                    if (uploads.isNotEmpty()) {
                        item {
                            Text(
                                text = "Uploads",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                                fontSize = 14.sp
                            )
                        }


                        items(uploads) {
                            TransferRow(Modifier.fillMaxWidth(), it) { onCancel(it.id) }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TransferRow(
    modifier: Modifier,
    operation: TransferOperation,
    onCancel: () -> Unit
) {

    Row(
        modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val (icon, transferName) =
            if (operation.transferType == TransferType.DOWNLOAD) Icons.Rounded.Download to "Downloading"
            else Icons.Rounded.Upload to "Uploading"


        val state = operation.transferState

        val progress = when (state) {
            is TransferState.Running -> {
                val p by state.progress.collectAsState()
                p.transferredBytes / p.totalBytes.toFloat()
            }

            is TransferState.Initializing -> {
                -1.0f
            }

            else -> {
                -1.0f
            }
        }

        val title = when (state) {
            is TransferState.Running -> {
                val p by state.progress.collectAsState()
                "$transferName ${p.currentTransferredFile}"
            }

            is TransferState.Finished -> {
                "Finished ${transferName.lowercase()} ${operation.resourceName}"
            }

            is TransferState.Failed -> {
                state.error.toString()//$transferName ${operation.resourceName} failed"
            }

            is TransferState.Initializing -> {
                "Setting up ${transferName.lowercase()}"
            }

            else -> "$transferName ${operation.resourceName} cancelled"
        }

        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            Modifier
                .weight(1f)
                .padding(bottom = 6.dp)
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (progress == -1.0f)
                LinearProgressIndicator(strokeCap = StrokeCap.Round)
            else
                LinearProgressIndicator(
                    progress,
                    strokeCap = StrokeCap.Round,
                    trackColor = ProgressIndicatorDefaults.linearColor.copy(alpha = 0.3f),
                )
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            onClick = onCancel, modifier = Modifier.size(28.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
        }
    }

}
