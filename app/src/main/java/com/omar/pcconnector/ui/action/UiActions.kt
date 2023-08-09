package com.omar.pcconnector.ui.action

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


/**
 * Represents an action that is displayed on overflow menus and toolbars
 */
data class ResourceAction(
    val actionName: String,
    val actionIcon: ImageVector?,
    val isDangerous: Boolean,
    val onClick: () -> Unit
)

/**
 * Actions used in the app
 */
object Actions {
    fun deleteAction(onClick: () -> Unit) = ResourceAction("Delete", Icons.Rounded.Delete, true, onClick)
    fun renameAction(onClick: () -> Unit) = ResourceAction("Rename", Icons.Rounded.Edit, false, onClick)
    fun copyAction(onClick: () -> Unit) = ResourceAction("Copy", Icons.Rounded.ContentCopy, false, onClick)
    fun downloadAction(onClick: () -> Unit) = ResourceAction("Download", Icons.Rounded.Download, false, onClick)
    fun shutdownPCAction(onClick: () -> Unit) = ResourceAction("Shutdown PC", Icons.Rounded.Power, true, onClick)
    fun lockPCAction(onClick: () -> Unit) = ResourceAction("Lock PC", Icons.Rounded.Lock, false, onClick)
    fun openLinkAction(onClick: () -> Unit) = ResourceAction("Open Link in Browser", Icons.Rounded.Web, false, onClick)
    fun copyToClipboardAction(onClick: () -> Unit) = ResourceAction("Copy to PC Clipboard", Icons.Rounded.ContentCopy, false, onClick)
}


/**
 * Draws a typical dropdown menu with the provided actions
 */
@Composable
fun ActionsDropdownMenu(
    modifier: Modifier = Modifier,
    actions: List<ResourceAction>,
    show: Boolean,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(modifier = modifier, expanded = show, onDismissRequest = onDismissRequest) {
        actions.forEach {

            val backgroundColor = Color.Transparent//if (it.isDangerous) Color(0xFFE91E63) else Color.Transparent

            DropdownMenuItem(
                modifier = Modifier.background(backgroundColor),
                text = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        it.actionIcon?.let { icon ->
                            Icon(imageVector = icon, contentDescription = "")
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = it.actionName)
                    }

                }, onClick = it.onClick
            )

        }
    }
}