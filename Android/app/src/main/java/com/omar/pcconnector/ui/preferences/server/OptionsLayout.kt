package com.omar.pcconnector.ui.preferences.server

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun BasicOptionSkeleton(
    modifier: Modifier,
    title: @Composable ColumnScope.() -> Unit,
    subtitle: @Composable ColumnScope.() -> Unit,
    value: @Composable RowScope.() -> Unit
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {
            title()
            subtitle()
        }

        value()
    }

}

@Composable
fun SwitchOption(
    modifier: Modifier,
    title: @Composable ColumnScope.() -> Unit,
    subtitle: @Composable ColumnScope.() -> Unit,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    BasicOptionSkeleton(
        modifier = Modifier
            .clickable { onToggle() }
            .then(modifier),
        title = title,
        subtitle = subtitle,
    ) {
        Switch(checked = isEnabled, onCheckedChange = { onToggle() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChoiceOption(
    modifier: Modifier = Modifier,
    title: @Composable ColumnScope.() -> Unit,
    subtitle: @Composable ColumnScope.() -> Unit,
    values: List<String>,
    selectedValue: String,
    onValueSelected: (index: Int) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    BasicOptionSkeleton(
        modifier = modifier,
        title = title,
        value = {},
        subtitle = { // we add it in the subtitle slot to put it in a new line

            Column(modifier = Modifier.fillMaxWidth()) {
                subtitle()

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded,
                    onExpandedChange = { expanded = it }) {

                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        value = selectedValue,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {

                        values.forEachIndexed { index, value ->
                            DropdownMenuItem(
                                text = { Text(text = value) },
                                onClick = {
                                    onValueSelected(index); expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }

                    }

                }
            }
        })
}