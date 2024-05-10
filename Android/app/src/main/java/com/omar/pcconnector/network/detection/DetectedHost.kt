package com.omar.pcconnector.network.detection

import com.omar.pcconnector.network.api.secureClient
import com.omar.pcconnector.network.connection.Connection
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


data class DetectedHost(val uuid: String, val serverName: String, val os: String, val ipAddress: String, val port: Int) {

    val okHTTPClient = OkHttpClient.Builder()
        .connectionSpecs(listOf(
            ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .allEnabledTlsVersions()
                .allEnabledCipherSuites()
                .build()
        )
        ).build()
    fun toConnection(): Connection = Connection(
        serverName,
        ipAddress,
        port,
        Retrofit.Builder().client(secureClient.build()).baseUrl("https://$ipAddress:$port")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    )
}
