package com.omar.pcconnector.network.api

import com.omar.pcconnector.network.connection.Connection
import kotlin.io.path.Path
import kotlin.io.path.name


fun getDownloadURL(connection: Connection, filePath: String): String {
    return connection.retrofit.baseUrl().toString() + "downloadFiles?src=$filePath"
}


fun getExternalDownloadURL(connection: Connection, filePath: String, accessToken: Token) =
     "http://${connection.ip}:${connection.port + 1}/download/${Path(filePath).name}?path=$filePath&token=$accessToken"