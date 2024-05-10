package com.omar.pcconnector.network.api

import okhttp3.ResponseBody
import retrofit2.http.GET



interface StatusAPI {
    @GET("/status")
    suspend fun status(): ResponseBody
}