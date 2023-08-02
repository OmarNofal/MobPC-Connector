package com.omar.pcconnector.model

import com.omar.pcconnector.network.api.secureClient
import com.omar.pcconnector.network.connection.Connection
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Info of some server
 */
data class DeviceInfo(
    val id: String,
    val name: String,
    val os: String
)

/**
 * A server which the user connected to before and contains the token
 */
data class PairedDevice(
    val deviceInfo: DeviceInfo,
    val token: String,
    val autoConnect: Boolean
)

/**
 * A device which is detected but not yet connected to
 */
data class DetectedDevice(
    val deviceInfo: DeviceInfo,
    val ip: String,
    val port: Int
) {

    fun toConnection(): Connection = Connection(
        deviceInfo.name,
        ip,
        port,
        Retrofit.Builder().client(secureClient.build()).baseUrl("https://$ip:$port")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    )
}