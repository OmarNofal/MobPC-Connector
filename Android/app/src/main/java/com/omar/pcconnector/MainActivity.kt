package com.omar.pcconnector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.pairing.PairingScreen
import com.omar.pcconnector.preferences.LocalUserPreferences
import com.omar.pcconnector.preferences.UserPreferencesRepository
import com.omar.pcconnector.ui.empty.EmptyDevicesScreen
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.main.MainApp
import com.omar.pcconnector.ui.nav.EmptyScreen
import com.omar.pcconnector.ui.nav.MainScreen
import com.omar.pcconnector.ui.nav.PairingScreen
import com.omar.pcconnector.ui.nav.Screen
import com.omar.pcconnector.ui.preferences.PreferencesScreen
import com.omar.pcconnector.ui.theme.AppTheme
import com.omar.pcconnector.ui.theme.isDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var eventsFlow: MutableSharedFlow<ApplicationEvent>

    @Inject
    lateinit var devicesRepository: DevicesRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)


        val initialDevices =
            runBlocking { devicesRepository.getAllPairedDevices() }

        Log.d("Initial Devices: ", initialDevices.toString())


        setContent {

            val navController = rememberNavController()

            val prefs by userPreferencesRepository.preferences.collectAsState()

            CompositionLocalProvider(LocalUserPreferences provides prefs) {

                AppTheme(
                    useDarkTheme = LocalUserPreferences.current.appTheme.isDarkTheme()
                ) {

                    val currentDevices: List<PairedDevice> by
                    devicesRepository.getPairedDevicesFlow()
                        .collectAsState(initial = initialDevices)

                    NavHost(
                        navController = navController,
                        startDestination = if (initialDevices.isEmpty()) EmptyScreen else MainScreen(
                            null
                        )
                    ) {

                        composable<EmptyScreen> {

                            EmptyDevicesScreen(modifier = Modifier.fillMaxSize()) {
                                navController.navigate(PairingScreen)
                            }

                        }

                        composable<MainScreen> {

                            MainApp(
                                modifier = Modifier.fillMaxSize(),
                                pairedDevices = currentDevices,
                                devicesRepository = devicesRepository,
                                eventsFlow = eventsFlow
                            )

                        }

                        composable<PairingScreen> {

                            PairingScreen(
                                onPairedWithDevice = { id ->
                                    navController.navigate(MainScreen(id))
                                },
                                onNavigateBack = navController::popBackStack
                            )

                        }

                    }


                }
            }
        }
    }
}