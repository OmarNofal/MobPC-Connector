package com.omar.pcconnector.pairing




sealed interface PairingScreenState {

    object Scanning: PairingScreenState

    data class Connecting(
        val name: String
    ): PairingScreenState

}
