package com.omar.pcconnector.network.api

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


private val trustManager = object : X509TrustManager {
    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

    }

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }

}

private val sslContext = SSLContext.getInstance("SSL").apply {
    init(null, arrayOf(trustManager), SecureRandom())
}


val secureClient = OkHttpClient.Builder()
    .sslSocketFactory(sslContext.socketFactory, trustManager)
    .hostnameVerifier { _, _ -> true }
