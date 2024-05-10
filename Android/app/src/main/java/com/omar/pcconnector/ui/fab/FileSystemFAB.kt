package com.omar.pcconnector.ui.fab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DriveFolderUpload
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun FileSystemFAB(
    onMakeDir: () -> Unit,
    onUploadFolder: () -> Unit,
    onUploadFile: () -> Unit,
    onPaste: (() -> Unit)?
) {
    var isShown by remember { mutableStateOf(false) }

    val items = mutableListOf(
        FabAction("Create a Folder", Icons.Rounded.CreateNewFolder) { isShown = false; onMakeDir() },
        FabAction("Upload a Folder", Icons.Rounded.DriveFolderUpload) { isShown = false; onUploadFolder() },
        FabAction("Upload a File", Icons.Rounded.UploadFile) { isShown = false; onUploadFile() },
    )

    if (onPaste != null)
        items.add(FabAction("Paste", Icons.Rounded.ContentPaste, onPaste))

    MultiItemFab(
        icon = Icons.Rounded.Add,
        onClicked = { isShown = !isShown },
        expanded = isShown,
        items = items,
        showRainbowBorder = onPaste != null
    ) {
        isShown = false
    }


}