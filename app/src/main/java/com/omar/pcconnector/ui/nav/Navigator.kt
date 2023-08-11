package com.omar.pcconnector.ui.nav

import android.net.Uri
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


interface NavigationCommand {
    val options: NavOptions
    val screen: Screen

    val destination: String
}


const val BACK_COMMAND = "back"
val BackCommand = object : NavigationCommand {
    override val options: NavOptions
        get() = navOptions { }
    override val screen: Screen
        get() = throw IllegalStateException("No Screen for back command")
    override val destination: String
        get() = BACK_COMMAND
}

object ImageScreen {

    const val PATH_ARG = "path"
    const val IP_ARG = "ip"
    const val PORT_ARG = "port"
    const val TOKEN_ARG = "token"

    val arguments = listOf(
        navArgument(PATH_ARG) { type = NavType.StringType },
        navArgument(IP_ARG) { type = NavType.StringType },
        navArgument(PORT_ARG) { type = NavType.IntType },
        navArgument(TOKEN_ARG) { type = NavType.StringType }
    )

    fun navigationCommand(imageUrl: String, ip: String, port: Int, token: String) = object : NavigationCommand {
        override val options: NavOptions
            get() = navOptions { }
        override val screen: Screen
            get() = Screen.ImageScreen
        override val destination: String
            get() =
                "image/${Uri.encode(imageUrl)}/$ip/$port/$token"
    }

}

object ServerScreen {


    const val ID_ARG = "id"
    val arguments = listOf(navArgument(ID_ARG) { type = NavType.StringType })

    fun navigationCommand(id: String) = object : NavigationCommand {
        override val options: NavOptions
            get() = navOptions { }
        override val screen: Screen
            get() = Screen.ServerScreen
        override val destination: String
            get() = "server/$id"
    }

}

object DetectionScreen {

    fun navigationCommand() = object : NavigationCommand {
        override val options: NavOptions
            get() = navOptions { }
        override val screen: Screen
            get() = Screen.DetectionScreen
        override val destination: String
            get() = Screen.DETECTION_SCREEN
    }

}


class Navigator {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _navEvents = MutableSharedFlow<NavigationCommand>()
    val navigationEvents
        get() = _navEvents.asSharedFlow()

    fun navigate(navigationCommand: NavigationCommand) {
        scope.launch {
            _navEvents.emit(navigationCommand)
        }
    }

    fun goBack() {
        scope.launch {
            _navEvents.emit(BackCommand)
        }
    }

}