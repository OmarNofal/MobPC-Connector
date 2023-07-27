package com.omar.pcconnector.network.api

import com.omar.pcconnector.network.connection.Connection


fun getDownloadURL(connection: Connection, imagePath: String): String {
    return connection.retrofit.baseUrl().toString() + "downloadFiles?src=$imagePath"
}