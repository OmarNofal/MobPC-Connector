package com.omar.pcconnector.network.connection

import retrofit2.Retrofit


data class Connection(
    val serverName: String,
    val ip: String,
    val port: Int,
    val retrofit: Retrofit,
)