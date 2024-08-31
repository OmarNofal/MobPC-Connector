package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.preferences.LocalUserPreferences
import com.omar.pcconnector.preferences.ServerPreferences
import com.omar.pcconnector.ui.preferences.PreferencesGroupHeader


fun LazyListScope.singleServerPreferencesGroup(
    deviceId: String,
    preferencesActions: ServerPreferencesActions,
    onGoToNotificationsExcludedPackages: (String) -> Unit,

    ) = item {
    SingleServerPreferencesGroup(
        Modifier
            .fillMaxWidth(),
        deviceId,
        preferencesActions,
        onGoToNotificationsExcludedPackages

    )
}


@Composable
private fun SingleServerPreferencesGroup(
    modifier: Modifier,
    serverId: String,
    preferencesActions: ServerPreferencesActions,
    onGoToNotificationsExcludedPackages: (String) -> Unit
) {

    val preferences = LocalUserPreferences.current
    val serverPreferences = remember(preferences) {
        preferences.serversPreferencesList.find { it.serverId == serverId }
            ?: ServerPreferences.newBuilder().setServerId(serverId)
                .setStartPath("").build()
    }

    val optionModifier = remember {
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    }

    Column(modifier) {

        PreferencesGroupHeader(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp
            ),
            title = "Startup"
        )

        startupPreferencesGroup(
            optionModifier = optionModifier,
            startupPath = serverPreferences.startPath,
            isDefaultServer = preferences.defaultServerId == serverId,
            onStartupPathChange = preferencesActions::setStartPath,
            onSetAsDefault = preferencesActions::setAsDefault
        )

        HorizontalDivider()

        PreferencesGroupHeader(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp
            ),
            title = "File System"
        )

        fileSystemPreferencesGroup(
            optionModifier = optionModifier,
            showHiddenResource = serverPreferences.showHiddenResources,
            sortCriteria = serverPreferences.sortingCriteria,
            filesSeparation = serverPreferences.foldersAndFilesSeparation,
            onToggleHiddenResources = preferencesActions::toggleShowHiddenResource,
            onSortCriteriaChanged = preferencesActions::changeFileSystemSortCriteria,
            onFoldersAndFilesSeparationChange = preferencesActions::changeFilesAndFoldersSeparation
        )

        HorizontalDivider()

        PreferencesGroupHeader(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp
            ),
            title = "Notifications"
        )

        notificationsPreferencesGroup(
            optionModifier = optionModifier,
            sendNotifications = serverPreferences.sendPhoneNotificationsToServer,
            onToggleSendNotifications = preferencesActions::toggleSendPhoneNotificationsToServer,
            onNavigateToExcludedPackages = {
                onGoToNotificationsExcludedPackages(
                    serverId
                )
            }
        )

    }

}
