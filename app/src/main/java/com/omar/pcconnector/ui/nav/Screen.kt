package com.omar.pcconnector.ui.nav




/**
 * Represents the possible screens the user can navigate to
 */
sealed class Screen {


    object DetectionScreen: Screen()

    // main screen
    object ServerScreen: Screen()

    object ImageScreen: Screen()


    companion object {
        const val DETECTION_SCREEN = "detection"
        const val SERVER_SCREEN = "server/{id}"
        const val IMAGE_SCREEN = "image/{path}"
    }

}