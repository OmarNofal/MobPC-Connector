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
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.security.trustCertificate
import com.omar.pcconnector.ui.detection.DetectionScreen
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.nav.BackCommand
import com.omar.pcconnector.ui.nav.ImageScreen
import com.omar.pcconnector.ui.nav.Navigator
import com.omar.pcconnector.ui.nav.Screen
import com.omar.pcconnector.ui.nav.ServerScreen
import com.omar.pcconnector.ui.preview.ImagePreview
import com.omar.pcconnector.ui.session.ServerSession
import com.omar.pcconnector.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import java.nio.file.Paths
import javax.inject.Inject


const val CERT = """-----BEGIN CERTIFICATE-----
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
-----END CERTIFICATE-----
"""

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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ///trustCertificate(CERT)
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
                            val ip = it.arguments?.getString(ImageScreen.IP_ARG)
                            val port = it.arguments?.getInt(ImageScreen.PORT_ARG)
                            val token = it.arguments?.getString(ImageScreen.TOKEN_ARG)

                            if (listOf(ip, port, token).any { it == null }) {
                                navigator.goBack()
                            } else {
                                ImagePreview(
                                    getRetrofit(ip!!, port!!, token!!),
                                    Paths.get(resourcePath)
                                )
                                Log.i("Resource", resourcePath.toString())
                            }

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
