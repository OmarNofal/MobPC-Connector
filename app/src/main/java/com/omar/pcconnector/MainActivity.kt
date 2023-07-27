package com.omar.pcconnector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.omar.pcconnector.network.connection.Connection
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
import java.nio.file.Paths
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var eventsFlow: MutableSharedFlow<ApplicationEvent>

    @Inject
    lateinit var navigator: Navigator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var currentConnection by  mutableStateOf<Connection?>(null)

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
                        DetectionScreen(onConnectionSelected = {
                            currentConnection = it
                            navigator.navigate(ServerScreen.navigationCommand())
                        })
                    }

                    composable(Screen.SERVER_SCREEN) {
                        ServerSession(modifier = Modifier.fillMaxSize(), currentConnection!!, eventsFlow)
                    }

                    composable(
                        Screen.IMAGE_SCREEN,
                        arguments = ImageScreen.arguments,
                    ) {
                        val resourcePath = it.arguments?.getString(ImageScreen.PATH_ARG)
                        Log.i("Resource", resourcePath.toString())
                        ImagePreview(currentConnection!!, Paths.get(resourcePath))
                    }
                }

            }
        }
    }


}
