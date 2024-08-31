package com.omar.pcconnector.ui.preferences.server

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import com.omar.pcconnector.ui.preferences.PreferenceSubtitle
import com.omar.pcconnector.ui.preferences.PreferenceTitle


@Composable
fun ColumnScope.notificationsPreferencesGroup(
    optionModifier: Modifier,
    sendNotifications: Boolean,
    onToggleSendNotifications: () -> Unit,
    onNavigateToExcludedPackages: () -> Unit
) {

    SendNotificationsPreference(
        modifier = optionModifier,
        enabled = sendNotifications,
        onToggle = onToggleSendNotifications
    )

    ExcludedPackagesPreference(
        modifier = optionModifier,
        onClick = onNavigateToExcludedPackages
    )

}

@Composable
fun SendNotificationsPreference(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onToggle: () -> Unit
) {

    val context = LocalContext.current

    val onSwitch: () -> Unit = remember(enabled) {
        lambda@{
            if (enabled)
                return@lambda onToggle()

            val accessEnabled = context.isEnabledNotificationAccess()
            if (accessEnabled)
                onToggle()
            else
                context.sendToNotificationListenerPermission()
        }
    }
    SwitchOption(
        modifier = modifier,
        title = { PreferenceTitle(text = "Send Notifications") },
        subtitle = { PreferenceSubtitle(text = "Sends phone notifications to your server") },
        isEnabled = enabled,
        onToggle = onSwitch
    )
}

@Composable
fun ExcludedPackagesPreference(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    BasicOptionSkeleton(
        modifier = Modifier
            .clickable { onClick() }
            .then(modifier),
        title = {
            PreferenceTitle(text = "Excluded Applications")
        },
        subtitle = { PreferenceSubtitle(text = "Notifications that should not be sent") },
        value = {}
    )

}


private fun Context.isEnabledNotificationAccess(): Boolean =
    packageName in NotificationManagerCompat.getEnabledListenerPackages(this)

private fun Context.sendToNotificationListenerPermission() {
    Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
    val intent =
        Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    startActivity(intent)
}