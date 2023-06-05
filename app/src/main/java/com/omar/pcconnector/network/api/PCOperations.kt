package com.omar.pcconnector.network.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface PCOperations {
    @GET("/lockPc")
    fun lockPC(): Call<GeneralResponse<Unit>>

    @GET("/shutdownPc")
    fun shutdownPC(): Call<GeneralResponse<Unit>>

    @FormUrlEncoded
    @POST("/copyToClipboard")
    fun copyToClipboard(@Field("text") text: String): Call<GeneralResponse<Unit>>

    @FormUrlEncoded
    @POST("/openLink")
    fun openLink(@Field("url") url: String, @Field("incognito") incognito: Int): Call<GeneralResponse<Unit>>
}