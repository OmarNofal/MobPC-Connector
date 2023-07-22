package com.omar.pcconnector.ui.nav




/**
 * Represents the possible screens the user can navigate to
 */
sealed class Screen(val route: String) {


    object DetectionScreen: Screen(DETECTION_SCREEN)

    // main screen
    object ServerScreen: Screen(SERVER_SCREEN)

    object ImageScreen: Screen(IMAGE_SCREEN)


    companion object {
        const val DETECTION_SCREEN = "detection"
        const val SERVER_SCREEN = "server"
        const val IMAGE_SCREEN = "image/{resource}"
    }

}