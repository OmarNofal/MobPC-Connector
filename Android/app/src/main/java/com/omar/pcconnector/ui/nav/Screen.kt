package com.omar.pcconnector.ui.nav

import kotlinx.serialization.Serializable



@Serializable
object PairingScreen

@Serializable
data class MainScreen(val initialDevice: String?)

@Serializable
object EmptyScreen

@Serializable
sealed interface Screen {

    /**
     * A screen to allow the user to pair with their server
     * by scanning a QR code
     */
    @Serializable
    object PairingScreen : Screen


    /**
     * The main screen containing, all servers, and the screen
     * the user will interact with the most.
     *
     * Only shown if he is paired with at least one server
     */
    @Serializable
    data class MainScreen(val initialDevice: String?) : Screen

    /**
     * A nested screen inside [MainScreen] showing the file system
     * of the selected server
     */
    @Serializable
    data class ServerScreen(val serverId: String) : Screen

    /**
     * A nested screen inside [MainScreen] to preview an image
     * stored on a particular server
     */
    @Serializable
    data class ImagePreviewScreen(
        val ip: String,
        val port: Int,
        val certificate: String,
        val token: String,
        val imagePath: String
    ) : Screen

    /**
     * The settings screen
     */
    @Serializable
    object SettingsScreen : Screen


    /**
     * The screen shown to the user when he is not paired
     * with any server
     */
    @Serializable
    object EmptyScreen : Screen


}