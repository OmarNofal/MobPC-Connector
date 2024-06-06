package com.omar.pcconnector.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.ui.drawer.AppDrawer
import com.omar.pcconnector.ui.event.ApplicationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


/**
 * This is the view which contains the whole application
 * and is only shown when the user is paired to at least 1
 * device.
 */
@Composable
fun MainApp(
    modifier: Modifier,
    pairedDevices: List<PairedDevice>,
    devicesRepository: DevicesRepository,
    eventsFlow: MutableSharedFlow<ApplicationEvent>
) {

    if (pairedDevices.isEmpty()) return

    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    AppDrawer(
        modifier = modifier,
        pairedDevices = pairedDevices,
        selectedDeviceId = navController.getCurrentDeviceId(),
        drawerState = drawerState,
        onDeviceClicked = { deviceId ->
            navController.navigate("server/$deviceId")
            scope.launch { drawerState.close() }
        },
        onSettingsClicked = {
            navController.navigate(SETTINGS_ROUTE)
            scope.launch { drawerState.close() }
        }
    ) {

        NavHost(
            navController = navController,
            startDestination = "server/${pairedDevices.first().deviceInfo.id}",
            modifier = Modifier.fillMaxSize()
        ) {

            serverNavGraph(
                navController = navController,
                defaultDeviceId = pairedDevices.first().deviceInfo.id,
                devicesRepository = devicesRepository,
                eventsFlow = eventsFlow,
                onOpenDrawer = {
                    scope.launch { drawerState.open() }
                }
            )

            settingsScreen()

        }


    }


}

@Composable
fun NavController.getCurrentDeviceId(): String {

    val backStackEntry by currentBackStackEntryFlow
        .collectAsState(initial = null)

    return remember(backStackEntry) {
        val graph = this.currentBackStackEntry?.destination?.parent

        if (graph == null || graph.route != "server/{id}")
            return@remember ""

        return@remember (backStackEntry?.arguments?.getString("id")) ?: ""
    }
}