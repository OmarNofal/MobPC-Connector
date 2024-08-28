package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.omar.pcconnector.preferences.LocalUserPreferences
import com.omar.pcconnector.preferences.ServerPreferences
import com.omar.pcconnector.preferences.ServerPreferences.FileSystemSortCriteria
import com.omar.pcconnector.preferences.ServerPreferences.FoldersAndFilesSeparation
import com.omar.pcconnector.ui.preferences.PreferenceSubtitle
import com.omar.pcconnector.ui.preferences.PreferenceTitle


fun LazyListScope.singleServerPreferencesGroup(
    deviceId: String,
    preferencesActions: ServerPreferencesActions
) = item {
    SingleServerPreferencesGroup(
        Modifier
            .fillMaxWidth(),
        deviceId,
        preferencesActions
    )
}

@Composable
private fun SingleServerPreferencesGroup(
    modifier: Modifier,
    serverId: String,
    preferencesActions: ServerPreferencesActions
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

        StartPathPreference(
            modifier = optionModifier,
            value = serverPreferences.startPath,
            onValueChange = preferencesActions::setStartPath
        )

        DefaultServerPreference(
            modifier = optionModifier,
            isDefault = preferences.defaultServerId == serverId,
            onSetAsDefault = preferencesActions::setAsDefault
        )

        ShowHiddenResourcesOption(
            modifier = optionModifier,
            isEnabled = serverPreferences.showHiddenResources,
            onToggle = preferencesActions::toggleShowHiddenResource
        )

        SortCriteriaOption(
            modifier = optionModifier,
            sortCriteria = serverPreferences.sortingCriteria,
            onSortCriteriaChanged = preferencesActions::changeFileSystemSortCriteria
        )

        FoldersAndFileSeparationOption(
            modifier = optionModifier,
            value = serverPreferences.foldersAndFilesSeparation,
            onValueChange = preferencesActions::changeFilesAndFoldersSeparation
        )
    }

}

@Composable
private fun StartPathPreference(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {

    val text = remember(value) {
        TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        )
    }

    Column(
        modifier
    ) {

        Text(
            text = "Starting Path",
            style = MaterialTheme.typography.labelLarge
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            value = text,
            onValueChange = { onValueChange(it.text) }
        )

    }
}

@Composable
private fun DefaultServerPreference(
    modifier: Modifier,
    isDefault: Boolean,
    onSetAsDefault: () -> Unit
) {


    BasicOptionSkeleton(
        modifier = modifier,
        title = { PreferenceTitle(text = "Is default server?") },
        subtitle = { PreferenceSubtitle(text = "Auto-connect when app starts") }
    ) {
        if (isDefault) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0.0f, 0.8f, 0.0f)
            )
        } else {
            Button(onClick = onSetAsDefault) {
                Text(text = "Set as Default")
            }
        }

    }


}

@Composable
fun ShowHiddenResourcesOption(
    modifier: Modifier,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {

    SwitchOption(
        modifier = modifier,
        title = {
            PreferenceTitle(
                text = "Show hidden resources"
            )
        },
        subtitle = {},
        isEnabled = isEnabled,
        onToggle = onToggle
    )

}

@Composable
fun SortCriteriaOption(
    modifier: Modifier,
    sortCriteria: FileSystemSortCriteria,
    onSortCriteriaChanged: (FileSystemSortCriteria) -> Unit
) {

    val values = remember {
        listOf(
            FileSystemSortCriteria.NAME,
            FileSystemSortCriteria.SIZE,
            FileSystemSortCriteria.MODIFICATION_DATE
        )
    }

    val choices = remember {
        listOf(
            "Name",
            "File size",
            "Modification date"
        )
    }

    MultiChoiceOption(
        modifier = modifier,
        title = { PreferenceTitle(text = "Files display order") },
        subtitle = {},
        values = choices,
        selectedValue = choices[values.indexOf(sortCriteria)],
        onValueSelected = { onSortCriteriaChanged(values[it]) }
    )

}

@Composable
fun FoldersAndFileSeparationOption(
    modifier: Modifier,
    value: FoldersAndFilesSeparation,
    onValueChange: (FoldersAndFilesSeparation) -> Unit
) {

    val values = remember {
        listOf(
            FoldersAndFilesSeparation.FOLDERS_FIRST,
            FoldersAndFilesSeparation.FILES_FIRST,
            FoldersAndFilesSeparation.DEFAULT
        )
    }

    val choices = remember {
        listOf(
            "Folders first",
            "Files first",
            "Don't separate"
        )
    }

    MultiChoiceOption(
        modifier = modifier,
        title = { PreferenceTitle(text = "Resource grouping") },
        subtitle = { PreferenceSubtitle(text = "Separating files and folders") },
        values = choices,
        selectedValue = choices[values.indexOf(value)],
        onValueSelected = { onValueChange(values[it]) }
    )

}







