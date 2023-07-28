package com.omar.pcconnector.network.api

import com.omar.pcconnector.network.connection.Connection


fun getDownloadURL(connection: Connection, filePath: String): String {
    return connection.retrofit.baseUrl().toString() + "downloadFiles?src=$filePath"
}