package com.omar.pcconnector.ui.main

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.getRetrofit
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.fs.slidingEnterFromDown
import com.omar.pcconnector.ui.fs.slidingEnterFromEndAnimation
import com.omar.pcconnector.ui.fs.slidingEnterFromStartAnimation
import com.omar.pcconnector.ui.fs.slidingEnterFromUp
import com.omar.pcconnector.ui.fs.slidingExitToDown
import com.omar.pcconnector.ui.fs.slidingExitToEndAnimation
import com.omar.pcconnector.ui.fs.slidingExitToStartAnimation
import com.omar.pcconnector.ui.fs.slidingExitToUp
import com.omar.pcconnector.ui.nav.Screen
import com.omar.pcconnector.ui.preferences.PreferencesScreen
import com.omar.pcconnector.ui.preferences.server.NotificationsExcludedPackagesScreen
import com.omar.pcconnector.ui.preferences.server.ServerPreferencesScreen
import com.omar.pcconnector.ui.preview.ImagePreview
import com.omar.pcconnector.ui.session.ServerSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import java.nio.file.Paths


const val SETTINGS_ROUTE = "settings"

const val SERVER_ROUTE = "server/{id}"

const val FS_ROUTE = "fs"
const val IMAGE_ROUTE = "image/{ip}/{port}/{certificate}/{token}/{path}"

fun NavGraphBuilder.serverNavGraph(
    navController: NavController,
    defaultDeviceId: String,
    devicesRepository: DevicesRepository,
    eventsFlow: MutableSharedFlow<ApplicationEvent>,
    onOpenDrawer: () -> Unit,
) {

    navigation(
        startDestination = FS_ROUTE,
        route = SERVER_ROUTE,
        arguments = listOf(
            navArgument("id") {
                type = NavType.StringType;
            }
        )
    ) {

        /**
         * Shows the file system of the server
         */
        composable(
            FS_ROUTE,
            popEnterTransition = { slidingEnterFromUp },
            exitTransition = { slidingExitToUp }) { backStackEntry ->

            val id =
                backStackEntry.arguments?.getString("id") ?: defaultDeviceId

            Log.d("Device ID: ", id)

            val device =
                runBlocking { devicesRepository.getPairedDevice(id) }


            ServerSession(
                modifier = Modifier.fillMaxSize(),
                pairedDevice = device,
                eventsFlow = eventsFlow,
                onOpenDrawer = onOpenDrawer,
                onImageClicked = { conn, path ->
                    Log.e("IMAGE", conn.toString())
                    val route =
                        "image/${conn.ip}/${conn.port}/${Uri.encode(device.certificate)}/${device.token}/${
                            Uri.encode(path)
                        }"
                    navController.navigate(route)
                }
            )

        }

        /**
         * Composable for displaying an image on a server
         */
        composable(
            IMAGE_ROUTE,
            arguments = listOf(
                navArgument("ip") { type = NavType.StringType },
                navArgument("port") { type = NavType.IntType },
                navArgument("certificate") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType },
                navArgument("path") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val ip = backStackEntry.arguments?.getString("ip")!!
            val port = backStackEntry.arguments?.getInt("port")!!
            val cert =
                Uri.decode(backStackEntry.arguments?.getString("certificate"))!!
            val token = backStackEntry.arguments?.getString("token")!!

            val retrofit =
                getRetrofit(
                    ip = ip,
                    port = port,
                    certificate = cert,
                    token = token
                )

            val imagePath =
                Paths.get(Uri.decode(backStackEntry.arguments?.getString("path")!!))

            ImagePreview(
                retrofit = retrofit,
                imagePath = imagePath,
                onCloseScreen = navController::popBackStack
            )

        }

    }

}


fun NavGraphBuilder.settingsScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit
) {

    composable<Screen.SettingsScreen>(
        exitTransition = { slidingExitToStartAnimation },
        popEnterTransition = { slidingEnterFromStartAnimation },
        enterTransition = { slidingEnterFromDown },
        popExitTransition = { slidingExitToDown }
    ) {
        PreferencesScreen(openDrawer = onOpenDrawer, onDeviceClicked = {
            navController.navigate(Screen.ServerSettingsScreen(it.deviceInfo.id))
        })
    }

    navigation<Screen.ServerSettingsScreen>(
        startDestination = Screen.MainServerPreferences
    ) {

        composable<Screen.MainServerPreferences>(
            enterTransition = { slidingEnterFromEndAnimation },
            popExitTransition = { slidingExitToEndAnimation }
        ) {
            ServerPreferencesScreen(
                onDeviceDeleted = { navController.popBackStack() },
                onBackPressed = navController::popBackStack,
                onGoToNotificationsExcludedPackages = {
                    navController.navigate(
                        Screen.NotificationsExcludedPackagesScreen(
                            serverId = it
                        )
                    )
                }
            )
        }

        composable<Screen.NotificationsExcludedPackagesScreen> {

            NotificationsExcludedPackagesScreen(
                onBackPressed = navController::popBackStack
            )

        }

    }

}