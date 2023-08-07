package com.omar.pcconnector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.ui.detection.DetectionScreen
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.nav.BackCommand
import com.omar.pcconnector.ui.nav.ImageScreen
import com.omar.pcconnector.ui.nav.Navigator
import com.omar.pcconnector.ui.nav.Screen
import com.omar.pcconnector.ui.nav.ServerScreen
import com.omar.pcconnector.ui.session.ServerSession
import com.omar.pcconnector.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var eventsFlow: MutableSharedFlow<ApplicationEvent>

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var devicesRepository: DevicesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()

            LaunchedEffect(key1 = Unit) {
                navigator.navigationEvents.collect { command ->
                    if (command == BackCommand) navController.popBackStack()
                    else navController.navigate(command.destination)
                }
            }

            AppTheme {

                NavHost(navController = navController, startDestination = Screen.DETECTION_SCREEN) {

                    composable(Screen.DETECTION_SCREEN) {
                        DetectionScreen()
                    }




                    navigation(
                        startDestination = "server",
                        route = Screen.SERVER_SCREEN,
                        arguments = ServerScreen.arguments
                    ) {


                        composable("server") {
                            val deviceId: String =
                                it.arguments?.getString(ServerScreen.ID_ARG) ?: ""
                            val deviceState by remember {
                                devicesRepository.getPairedDeviceFlow(deviceId)
                                    .map { device ->
                                        DeviceLoadingState.Loaded(device)
                                    }
                            }.collectAsState(initial = DeviceLoadingState.Loading)

                            val device = (deviceState as? DeviceLoadingState.Loaded)?.pairedDevice
                            if (device == null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                ServerSession(modifier = Modifier.fillMaxSize(), device, eventsFlow)
                            }
                        }



                        composable(
                            Screen.IMAGE_SCREEN,
                            arguments = ImageScreen.arguments,
                        ) {
                            val resourcePath = it.arguments?.getString(ImageScreen.PATH_ARG)
                            Log.i("Resource", resourcePath.toString())
                            //ImagePreview(null, Paths.get(resourcePath))
                        }

                    }


                }


            }
        }
    }

    sealed class DeviceLoadingState {
        object Loading : DeviceLoadingState()
        class Loaded(val pairedDevice: PairedDevice) : DeviceLoadingState()
    }

}
