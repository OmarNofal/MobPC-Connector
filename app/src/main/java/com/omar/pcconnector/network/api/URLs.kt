package com.omar.pcconnector.network.api

import com.omar.pcconnector.network.connection.Connection


fun getDownloadURL(connection: Connection, filePath: String): String {
    return connection.retrofit.baseUrl().toString() + "downloadFiles?src=$filePath"
}


fun getExternalDownloadURL(connection: Connection, filePath: String, accessToken: Token) =
    connection.retrofit.baseUrl().toString() + "getFileExternal?path=$filePath&token=$accessToken"