package com.omar.pcconnector.ui.fs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.nio.file.Path
import java.nio.file.Paths


@Composable
fun LocationBar(
    modifier: Modifier,
    path: String,
    drives: List<String>,
    onPathSelected: (Path) -> Unit,
    toggleSearchFilter: () -> Unit,
    isSearchFilterEnabled: Boolean,
    searchFilter: String,
    onSearchFilterChanged: (String) -> Unit
) {

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 1) Text bar with the dropdown button
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            var isPathEditable by rememberSaveable { mutableStateOf(false) }

            if (isSearchFilterEnabled) {
                BackHandler(true) {
                    toggleSearchFilter()
                }
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = searchFilter,
                    onValueChange = onSearchFilterChanged,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    singleLine = true,
                )
                LaunchedEffect(key1 = Unit) { focusRequester.requestFocus() }
            } else if (isPathEditable) {
                var editedPath by remember {
                    mutableStateOf(
                        TextFieldValue(
                            text = path,
                            selection = TextRange(path.length)
                        )
                    )
                }
                val focusRequester = remember { FocusRequester() }

                val resourceIcon =
                    if (path in drives) Icons.Outlined.Storage
                    else Icons.Outlined.Folder

                Icon(
                    imageVector = resourceIcon,
                    contentDescription = "Disk",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                LocationEditText(
                    modifier = Modifier.weight(1f),
                    path = editedPath,
                    onPathChanged = { editedPath = it },
                    focusRequester = focusRequester,
                    onCancel = {
                        isPathEditable = false
                    }
                ) {
                    if (editedPath.text.isNotBlank())
                        onPathSelected(Paths.get(editedPath.text))
                    isPathEditable = false
                }

                LaunchedEffect(key1 = Unit) {
                    focusRequester.requestFocus()
                }
            } else
                LocationIdleView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    isPathEditable = true
                                }
                            )
                        },
                    path = Paths.get(path),
                    drives,
                    { onPathSelected(Paths.get(it)) },
                    onPathSelected
                )

            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Navigation Dropdown"
                )
            }
        }


//        Divider(
//            Modifier
//                .fillMaxHeight()
//                .width(1.dp)
//        )

        // 2) Refresh button
        Box(
            Modifier
                .width(48.dp)
                .fillMaxHeight()
                .clickable { toggleSearchFilter() },
            contentAlignment = Alignment.Center
        ) {
            val icon =
                if (isSearchFilterEnabled) Icons.Rounded.Close else Icons.Rounded.Search
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = icon,
                contentDescription = "Search directory"
            )
        }


    }


}


@Composable
fun LocationIdleView(
    modifier: Modifier,
    path: Path,
    drives: List<String>,
    onDriveSelected: (String) -> Unit,
    onSubPathClicked: (Path) -> Unit
) {
    val scrollState = rememberLazyListState()
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {

        Box {

            var expanded by remember { mutableStateOf(false) }
            AssistChip(onClick = { expanded = !expanded }, label = {
                Text(text = path.first().toString())
            })

            DrivesDropdown(
                modifier = Modifier,
                expanded = expanded,
                drives = drives,
                onDismissRequest = { expanded = false },
                onDriveSelected = { expanded = false; onDriveSelected(it) }
            )
        }

        LazyRow(
            Modifier
                .weight(1f)
                .padding(start = 8.dp), state = scrollState
        ) {
            item {
                Divider(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .padding(start = 2.dp)
                )
            }
            items(path.nameCount - 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .sizeIn(32.dp)
                            .fillMaxHeight()
                            .clickable {
                                onSubPathClicked(
                                    path.subpath(
                                        0,
                                        it + 2
                                    )
                                )
                            }
                            .padding(
                                start = 4.dp,
                                end = 4.dp,
                                top = 2.dp,
                                bottom = 2.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier,
                            text = path.getName(it + 1)
                                .toString(), // +1 to avoid listing the drive twice
                            maxLines = 1,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                    if (it != path.nameCount - 2) {
                        // TODO Support RTL
                        Icon(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(10.dp),
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(key1 = path) {
        delay(150L)
        if (isActive)
            scrollState.animateScrollToItem(path.nameCount, -1)
    }
}


@Composable
fun LocationEditText(
    modifier: Modifier,
    path: TextFieldValue,
    focusRequester: FocusRequester,
    onPathChanged: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
    onPathSubmitted: () -> Unit
) {
    BackHandler(true, onCancel)
    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = path,
        onValueChange = onPathChanged,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onPathSubmitted() }
        ),
        singleLine = true,
        maxLines = 1,
        textStyle = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}


@Composable
fun DrivesDropdown(
    modifier: Modifier,
    expanded: Boolean,
    drives: List<String>,
    onDismissRequest: () -> Unit,
    onDriveSelected: (String) -> Unit,
) {

    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        drives.forEach { drive ->
            DropdownMenuItem(
                text = { Text(text = drive) },
                onClick = { onDriveSelected(drive) })
        }
    }

}
