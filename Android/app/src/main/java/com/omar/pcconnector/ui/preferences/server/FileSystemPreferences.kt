package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.omar.pcconnector.preferences.ServerPreferences.FileSystemSortCriteria
import com.omar.pcconnector.preferences.ServerPreferences.FoldersAndFilesSeparation
import com.omar.pcconnector.ui.preferences.PreferenceSubtitle
import com.omar.pcconnector.ui.preferences.PreferenceTitle


@Composable
fun ColumnScope.fileSystemPreferencesGroup(
    optionModifier: Modifier,
    showHiddenResource: Boolean,
    sortCriteria: FileSystemSortCriteria,
    filesSeparation: FoldersAndFilesSeparation,
    onToggleHiddenResources: () -> Unit,
    onSortCriteriaChanged: (FileSystemSortCriteria) -> Unit,
    onFoldersAndFilesSeparationChange: (FoldersAndFilesSeparation) -> Unit
) {

    ShowHiddenResourcesOption(
        modifier = optionModifier,
        isEnabled = showHiddenResource,
        onToggle = onToggleHiddenResources
    )

    SortCriteriaOption(
        modifier = optionModifier,
        sortCriteria = sortCriteria,
        onSortCriteriaChanged = onSortCriteriaChanged
    )

    FoldersAndFileSeparationOption(
        modifier = optionModifier,
        value = filesSeparation,
        onValueChange = onFoldersAndFilesSeparationChange
    )

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







