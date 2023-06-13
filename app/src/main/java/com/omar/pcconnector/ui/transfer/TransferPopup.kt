package com.omar.pcconnector.ui.transfer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.pcconnector.model.TransferOperation
import com.omar.pcconnector.model.TransferProgress
import com.omar.pcconnector.model.TransferState
import com.omar.pcconnector.model.TransferType
import kotlinx.coroutines.flow.StateFlow


@Composable
fun TransferPopup(
    modifier: Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    transferViewModel: TransferViewModel = hiltViewModel(),
) {

    val transfers by transferViewModel.getTransfersFlow().collectAsState(initial = listOf())
    val alpha by animateFloatAsState(targetValue = if (visible) 0.8f else 0.0f)


    val surfaceModifier = if (visible) Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures {
                onDismiss()
            }
        }
    else Modifier.fillMaxSize()

    BackHandler(visible, onDismiss)

    Box(contentAlignment = Alignment.TopCenter, modifier = surfaceModifier.background(Color.Black.copy(alpha))) {
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(expandFrom = Alignment.Top) { 0 } + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) { 0 } + fadeOut(),
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

                val downloads =
                    transfers.filter { op -> op.transferType == TransferType.DOWNLOAD }
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
                        items(downloads, key = { it.id }) {
                            TransferRow(
                                Modifier.fillMaxWidth(),
                                it,
                                { onCancel(it.id) },
                                { onDelete(it.id) }
                            )
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


                        items(uploads, key = { it.id }) {
                            TransferRow(
                                Modifier.fillMaxWidth(),
                                it,
                                { onCancel(it.id) },
                                { onDelete(it.id) }
                            )
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
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {

    Row(
        modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        val icon = operationIcon(operation)

        val transferName =
            if (operation.transferType == TransferType.DOWNLOAD) "Downloading"
            else "Uploading"


        val state = operation.transferState

        val title = when (state) {
            is TransferState.Running -> {
                val p by state.progress.collectAsState()
                "$transferName ${p.currentTransferredFile}"
            }

            else -> operation.resourceName
        }

//            is TransferState.Finished -> {
//                "Finished ${transferName.lowercase()} ${operation.resourceName}"
//            }
//
//            is TransferState.Failed -> {
//                state.error.toString()//$transferName ${operation.resourceName} failed"
//            }
//
//            is TransferState.Initializing -> {
//                "Setting up ${transferName.lowercase()}"
//            }
//
//            else -> "$transferName ${operation.resourceName} cancelled"

        val iconColor =
            when (icon) {
                Icons.Filled.CheckCircle -> Color(0xFF408140)
                Icons.Filled.Cancel -> Color(0xFF814040)
                else -> Color.Unspecified
            }

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = iconColor
        )

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
            Subtext(state)
        }
        Spacer(modifier = Modifier.width(12.dp))

        val closeDeleteIcon = closeOrDeleteIcon(operation)
        val callback =
            if (closeDeleteIcon == Icons.Default.Close) onCancel
            else onDelete
        IconButton(
            onClick = callback
        ) {

            Icon(imageVector = closeDeleteIcon, contentDescription = "Cancel", modifier = Modifier.size(21.dp))
        }
    }

}


@Composable
fun Subtext(
    state: TransferState
) {
    val spacerValue = when(state) {
        is TransferState.Initializing, is TransferState.Running -> 6.dp
        else -> 0.dp
    }
    Spacer(modifier = Modifier.height(spacerValue))
    when (state) {
        is TransferState.Initializing -> LinearProgressIndicator(strokeCap = StrokeCap.Round)
        is TransferState.Running -> TransferProgressBar(state = state.progress)
        is TransferState.Finished -> Text(text = "Completed", fontWeight = FontWeight.Normal, fontSize = 10.sp)
        is TransferState.Cancelled -> Text(text = "Cancelled", fontWeight = FontWeight.Normal, fontSize = 10.sp)
        is TransferState.Failed -> Text(text = state.error.toErrorString(), fontWeight = FontWeight.Normal, fontSize = 10.sp)
    }
}


@Composable
fun TransferProgressBar(
    state: StateFlow<TransferProgress>
) {
    val progress by state.collectAsState()
    val percentage = when (val p = (progress.transferredBytes / progress.totalBytes.toFloat())) {
        Float.NaN -> 0.0f
        else -> p
    }

    LinearProgressIndicator(
        percentage,
        strokeCap = StrokeCap.Round,
        trackColor = ProgressIndicatorDefaults.linearColor.copy(alpha = 0.3f),
    )
}


fun operationIcon(
    operation: TransferOperation
) =
    if (operation.transferState is TransferState.Finished)
        Icons.Filled.CheckCircle
    else if (operation.transferState is TransferState.Failed)
        Icons.Filled.Cancel
    else if (operation.transferType == TransferType.UPLOAD)
        Icons.Rounded.Upload
    else
        Icons.Rounded.Download


fun closeOrDeleteIcon(
    operation: TransferOperation
) =
    if (operation.transferState is TransferState.Running || operation.transferState is TransferState.Initializing)
        Icons.Default.Close
    else
        Icons.Default.Delete