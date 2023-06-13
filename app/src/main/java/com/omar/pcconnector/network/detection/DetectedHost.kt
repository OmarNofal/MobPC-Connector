package com.omar.pcconnector.network.detection

import com.omar.pcconnector.network.connection.Connection
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


data class DetectedHost(val serverName: String, val ipAddress: String, val port: Int) {

    fun toConnection(): Connection = Connection(
        serverName,
        ipAddress,
        port,
        Retrofit.Builder().baseUrl("http://$ipAddress:$port")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    )
}
