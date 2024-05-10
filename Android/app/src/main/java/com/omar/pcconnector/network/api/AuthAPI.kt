package com.omar.pcconnector.network.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


typealias Token = String

interface AuthAPI {

    @POST("/pair")
    @FormUrlEncoded
    suspend fun pair(
        @Field("pairingToken") pairingToken: String,
        @Field("os") os: String,
        @Field("modelName") modelName: String
    ): GeneralResponse<LoginResponse>

    @GET("/verifyToken")
    suspend fun verifyToken(@Query("token") token: String): GeneralResponse<VerifyTokenResponse>

}