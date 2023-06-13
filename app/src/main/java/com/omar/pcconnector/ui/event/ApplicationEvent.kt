package com.omar.pcconnector.ui.event

enum class ApplicationEvent {

    PC_LOCKED,
    PC_SHUT_DOWN,
    COPIED_TO_CLIPBOARD,
    URL_OPENED;

    fun toMessage(): String =
        when (this) {
            PC_LOCKED -> "PC Locked successfully"
            PC_SHUT_DOWN -> "PC Shutdown started"
            COPIED_TO_CLIPBOARD -> "Data copied to the clipboard"
            URL_OPENED -> "URL Opened successfully"
        }


}