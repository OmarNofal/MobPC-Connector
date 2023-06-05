package com.omar.pcconnector.network.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming


interface FileSystemOperations {

    @GET("/listDirectory")
    fun getDirectoryStructure(@Query("path") path: String): Call<GeneralResponse<List<NetworkResource>>>

    @FormUrlEncoded
    @POST("/renameResource")
    fun renameResource(
        @Field("src") src: String,
        @Field("newName") newName: String,
        @Field("overwrite") overwrite: Int
    ): Call<GeneralResponse<Unit>>

    @FormUrlEncoded
    @POST("/deleteResources")
    fun deleteResource(
        @Field("src") resourcePath: String,
        @Field("permanentlyDelete") permanentlyDelete: Int
    ): Call<GeneralResponse<Unit>>

    @FormUrlEncoded
    @POST("/mkdirs")
    fun makeDirs(
        @Field("dest") path: String,
        @Field("name") directoryName: String
    ): Call<GeneralResponse<Unit>>


    @Streaming
    @GET("/downloadFiles")
    fun download(
        @Query("src") path: String
    ): Call<ResponseBody>

}