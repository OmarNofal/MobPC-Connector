package com.omar.pcconnector.pairing

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.omar.pcconnector.authApi
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.network.api.clientForSSLCertificate
import com.omar.pcconnector.network.api.getDataOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import javax.inject.Inject


@HiltViewModel
class PairingScreenViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository
) : ViewModel() {


    private val _state =
        MutableStateFlow<PairingScreenState>(PairingScreenState.Scanning)

    val state: StateFlow<PairingScreenState>
        get() = _state.asStateFlow()


    /**
     * Called when the camera detects a QR code.
     *
     * This method is going to verify that the QR code is correct,
     * and will attempt to pair with the server.
     *
     */
    suspend fun onQrCodeDetected(text: String): PairingResult {

        val payload = try {
            Gson().fromJson(text, PairingPayload::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("PAIRING", e.stackTraceToString())
            return PairingResult.InvalidQRCode
        }

        // correct message, attempt to connect

        _state.value = PairingScreenState.Connecting(payload.serverName)

        // try to reach the server through given
        // ip addresses
        val ipAddresses = payload.ipAddresses
        for (ip in ipAddresses) {
            val authApi = Retrofit.Builder()
                .baseUrl("https://$ip:${payload.port}")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    clientForSSLCertificate(
                        payload.certificate,
                        ip
                    ).newBuilder().connectTimeout(Duration.ofMillis(2000))
                        .build()
                )
                .build()
                .authApi()

            try {
                val pairingResponse = authApi.pair(
                    payload.pairingToken,
                    "Android ${Build.VERSION.RELEASE}",
                    "${Build.MANUFACTURER} ${Build.MODEL}"
                )

                val token = pairingResponse.getDataOrThrow()!!.token
                // store token and device in database
                devicesRepository.storeDevice(
                    PairedDevice(
                        deviceInfo = DeviceInfo(
                            payload.serverId,
                            payload.serverName,
                            payload.os
                        ),
                        token = token,
                        autoConnect = false,
                        certificate = payload.certificate
                    )
                )

                Log.e("PAIRING", token)
                return PairingResult.Success(
                    payload.serverId,
                    payload.serverName
                )
            } catch (e: IOException) {
                Log.e("PAIRING", e.stackTraceToString())
                continue
            } catch (e: Exception) {
                Log.e("PAIRING", e.stackTraceToString())
                continue
            }
        }

        // could not reach the server
        return PairingResult.ServerNotInLan
    }

    sealed class PairingResult {

        /**
         * Failed to access the server from the devices' LAN.
         *
         * The user should make sure they are connected to the same network
         */
        object ServerNotInLan : PairingResult()

        /**
         * The QR code is not valid
         */
        object InvalidQRCode : PairingResult()

        /**
         * Something unknown happened
         */
        object UnknownError : PairingResult()

        /**
         * The pairing operation was successful
         * and the server was stored in the database
         */
        data class Success(
            val serverId: String,
            val serverName: String
        ) : PairingResult()

    }

}