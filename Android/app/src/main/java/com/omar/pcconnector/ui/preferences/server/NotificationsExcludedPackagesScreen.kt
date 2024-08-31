package com.omar.pcconnector.ui.preferences.server

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage


@Composable
fun NotificationsExcludedPackagesScreen(
    viewModel: NotificationsExcludedPackagesViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {


    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    NotificationsExcludedPackagesScreen(
        state = state,
        onAddPackage = viewModel::addPackage,
        onRemovePackage = viewModel::removePackage,
        onBackPressed = onBackPressed,
        onSubmit = {
            viewModel.submitChanges()
            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsExcludedPackagesScreen(
    state: NotificationsExcludedPackagesViewModel.ScreenState,
    onAddPackage: (String) -> Unit,
    onRemovePackage: (String) -> Unit,
    onBackPressed: () -> Unit,
    onSubmit: () -> Unit
) {

    if (state !is NotificationsExcludedPackagesViewModel.ScreenState.Loaded) {
        LoadingScreen(Modifier.fillMaxSize())
        return
    }

    val excludedPackages = remember(state.excludedPackageNames) {
        val map = state.excludedPackageNames.groupBy { it }
        state.allPackages.filter { it.packageName in map }
    }

    val allPackages = remember(state.excludedPackageNames) {
        val map = state.excludedPackageNames.groupBy { it }
        state.allPackages.filter { it.packageName !in map }
    }

    val topAppBarState = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${excludedPackages.size} selected ") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }, scrollBehavior = topAppBarState
            )
        },
        floatingActionButton = {
            FAB(onClick = onSubmit)
        }
    ) { padding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            ExcludedPackagesRow(
                modifier = Modifier
                    .fillMaxWidth(),
                pkgs = excludedPackages,
                onRemovePackage = onRemovePackage
            )

            if (excludedPackages.isNotEmpty())
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

            PackagesList(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topAppBarState.nestedScrollConnection),
                packages = allPackages,
                onAddPackage = onAddPackage
            )
        }

    }

}

@Composable
fun ExcludedPackagesRow(
    modifier: Modifier,
    pkgs: List<NotificationsExcludedPackagesViewModel.Package>,
    onRemovePackage: (String) -> Unit
) {

    val state = rememberLazyListState()

    LaunchedEffect(key1 = pkgs) {
        state.animateScrollToItem((pkgs.size - 1).coerceAtLeast(0))
    }

    LazyRow(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        state = state
    ) {

        items(pkgs) {

            Column(
                Modifier
                    .clickable { onRemovePackage(it.packageName) }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Box(
                    contentAlignment = Alignment.TopEnd
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(38.dp)
                            .padding(4.dp),
                        model = it.icon,
                        contentDescription = "Icon"
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Text(
                    text = it.appName,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.labelSmall
                )

            }

        }

    }

}

@Composable
fun PackagesList(
    modifier: Modifier = Modifier,
    packages: List<NotificationsExcludedPackagesViewModel.Package>,
    onAddPackage: (String) -> Unit
) {

    var searchQuery by remember {
        mutableStateOf("")
    }

    val filteredPackages = remember(searchQuery, packages) {
        packages.filter {
            it.appName.contains(
                searchQuery,
                ignoreCase = true
            ) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
    ) {

        item {
            SearchField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = searchQuery
            ) {
                searchQuery = it
            }
        }

        items(filteredPackages) {
            ApplicationRow(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                pkg = it,
                onAdd = { onAddPackage(it.packageName) })

            HorizontalDivider(modifier = Modifier.padding(start = (48 + 16).dp))
        }

    }

}

@Composable
private fun FAB(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF45BF26),
        contentColor = Color.White
    ) {
        Icon(imageVector = Icons.Rounded.Done, contentDescription = "Save")
    }
}

@Composable
fun ApplicationRow(
    modifier: Modifier = Modifier,
    pkg: NotificationsExcludedPackagesViewModel.Package,
    onAdd: () -> Unit
) {

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {


        AsyncImage(
            model = pkg.icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = pkg.appName,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pkg.packageName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(onClick = onAdd) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add to excluded list",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }

}

@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {


        OutlinedTextField(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .weight(1.0f),
            value = value,
            onValueChange = onValueChange,
            maxLines = 1,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search"
                )
            },
            placeholder = { Text(text = "Search...") },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors().copy()
        )
    }


}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(strokeCap = StrokeCap.Round)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Loading all applications",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}