package com.omar.pcconnector.network.connection

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(
    private val token: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val newRequest = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $token")

        return chain.proceed(newRequest.build())
    }
}