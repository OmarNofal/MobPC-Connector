package com.omar.pcconnector.ui.fs

import android.content.Context
import android.icu.text.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader
import coil.request.ImageRequest
import com.omar.pcconnector.R
import com.omar.pcconnector.bytesToSizeString
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.clientForSSLCertificate
import com.omar.pcconnector.network.connection.TokenInterceptor
import com.omar.pcconnector.supportedImageExtension
import com.omar.pcconnector.ui.DeleteDialog
import com.omar.pcconnector.ui.RenameDialog
import com.omar.pcconnector.ui.action.Actions
import com.omar.pcconnector.ui.action.ActionsDropdownMenu
import com.omar.pcconnector.ui.session.LocalConnectionProvider
import com.omar.pcconnector.ui.theme.iconForExtension
import kotlin.io.path.extension


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResourceRow(
    modifier: Modifier,
    resource: Resource,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    onRename: (String, Boolean) -> Unit = { _, _ -> },
    onDelete: () -> Unit = {},
    onDownload: (DocumentFile) -> Unit,
    onCopied: () -> Unit
) {

    val context = LocalContext.current

    val downloadDirPickerContract = directoryPicker {
        Log.i("PICKED DIRECTORY", it.toString())
        if (it == null) return@directoryPicker
        else onDownload(DocumentFile.fromTreeUri(context, it)!!)
    }

    var showRenameDialog by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }


    Row(
        modifier
            .combinedClickable(
                onLongClick = {
                    onLongPress()
                    Toast
                        .makeText(context, "Long pressed", Toast.LENGTH_LONG)
                        .show()
                }
            ) {
                onClick()
            }
            .padding(end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ResourceIcon(
            Modifier
                .padding(horizontal = 28.dp)
                .size(26.dp),
            resource
        )

        //Spacer(modifier = Modifier.width(32.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = resource.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))

            val dateFormat = remember {
                DateFormat.getDateInstance()
            }

            val subText =
                if (resource is DirectoryResource) "${resource.numResources} items" else resource.size.bytesToSizeString() + "  •  ${
                    dateFormat.format(
                        resource.creationDateMs
                    )
                }"
            Text(
                text = subText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column {

            var isMenuOpen by remember { mutableStateOf(false) }
            IconButton(
                onClick = { isMenuOpen = !isMenuOpen },
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More Options"
                )
            }
            ActionsDropdownMenu(
                actions = listOf(
                    Actions.downloadAction {
                        downloadDirPickerContract.launch(
                            null
                        )
                    },
                    Actions.copyAction(onCopied),
                    Actions.renameAction { showRenameDialog = true },
                    Actions.deleteAction { showDeleteDialog = true },
                ),
                show = isMenuOpen,
                onDismissRequest = { isMenuOpen = false }
            )
        }
    }

    if (showRenameDialog)
        RenameDialog(
            oldName = resource.name,
            onConfirm = { newName, overwrite -> onRename(newName, overwrite) },
            onCancel = { showRenameDialog = false }
        )

    if (showDeleteDialog)
        DeleteDialog(
            fileName = resource.name,
            onConfirm = onDelete,
            onCancel = { showDeleteDialog = false }
        )

}


@Composable
fun ResourceIcon(
    modifier: Modifier,
    resource: Resource
) {
    if (resource is DirectoryResource) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = "Directory icon",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurface
        )
    } else if (resource.path.extension.lowercase() in supportedImageExtension) {
        ImagePreviewIcon(
            modifier = modifier
                .clip(RoundedCornerShape(4.dp)),
            resource = resource
        )
    } else {
        Icon(
            painter = painterResource(id = iconForExtension(resource.path.extension)),
            contentDescription = "Directory icon",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}


fun imageLoader(
    context: Context,
    token: String,
    certificate: String,
    hostname: String
): ImageLoader {
    val client = clientForSSLCertificate(certificate, hostname).newBuilder()
        .addInterceptor(TokenInterceptor(token))
        .build()
    return ImageLoader.Builder(context)
        .okHttpClient(client)
        .crossfade(true)
        .build()
}

@Composable
fun ImagePreviewIcon(
    modifier: Modifier,
    resource: Resource
) {
    val connection = LocalConnectionProvider.current
    val resourceURL =
        "https://${connection.ip}:${connection.port}/downloadFiles?src=${resource.path}"
    val loader = LocalImageLoader.current
    Log.i("RESOURCE URL IMAGE", resourceURL)

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(resourceURL)
            .crossfade(true)
            .build(),

        placeholder = painterResource(R.drawable.image),
        error = painterResource(R.drawable.image),
        fallback = painterResource(R.drawable.image),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        imageLoader = loader
    )
}
