package com.omar.pcconnector.ui.nav

import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.google.gson.Gson
import com.omar.pcconnector.model.Resource
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
val BackCommand = object: NavigationCommand {
    override val options: NavOptions
        get() = navOptions {  }
    override val screen: Screen
        get() = throw IllegalStateException("No Screen for back command")
    override val destination: String
        get() = BACK_COMMAND
}

object ImageScreen {

    private const val RESOURCE_ARG = "resource"
    val arguments = listOf(navArgument(RESOURCE_ARG) { type = NavType.StringType })

    fun navigationCommand(resource: Resource) = object : NavigationCommand {
        override val options: NavOptions
            get() = navOptions { }
        override val screen: Screen
            get() = Screen.ImageScreen
        override val destination: String
            get() = "image/${Gson().toJson(resource)}"
    }

}

object ServerScreen {

    fun navigationCommand() = object : NavigationCommand {
        override val options: NavOptions
            get() = navOptions { }
        override val screen: Screen
            get() = Screen.ServerScreen
        override val destination: String
            get() = Screen.SERVER_SCREEN
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