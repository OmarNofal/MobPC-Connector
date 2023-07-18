package com.omar.pcconnector.network.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming


interface FileSystemOperations {

    @GET("/listDirectory")
    fun getDirectoryStructure(@Query("path") path: String): Call<GeneralResponse<List<NetworkResource>>>

    @GET("/drives")
    suspend fun getDrives(): GeneralResponse<List<String>>

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

    @FormUrlEncoded
    @POST("/copyResources")
    fun copyResources(
        @Field("src") src: String,
        @Field("dest") dest: String,
        @Field("overwrite") overwrite: Int = 0
    ): Call<GeneralResponse<Unit>>

    @Streaming
    @GET("/downloadFiles")
    suspend fun download(
        @Query("src") path: String
    ): retrofit2.Response<ResponseBody>

    @Multipart
    @POST("/uploadFiles")
    suspend fun upload(
        @Part parts: List<MultipartBody.Part>
    ): GeneralResponse<Unit>
}