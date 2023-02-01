package com.omar.pcconnector

import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET


interface Api {

    @GET("/lockPc")
    fun lockPc(): Call<ResponseBody>

    @GET("/status")
    fun status(): Call<ResponseBody>

}