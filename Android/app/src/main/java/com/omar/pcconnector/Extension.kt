package com.omar.pcconnector

import com.omar.pcconnector.network.api.AuthAPI
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.api.PCOperations
import com.omar.pcconnector.network.api.StatusAPI
import retrofit2.Retrofit


fun Retrofit.fileSystemApi(): FileSystemOperations = create(FileSystemOperations::class.java)
fun Retrofit.statusApi(): StatusAPI = create(StatusAPI::class.java)
fun Retrofit.pcApi(): PCOperations = create(PCOperations::class.java)
fun Retrofit.authApi(): AuthAPI = create(AuthAPI::class.java)
