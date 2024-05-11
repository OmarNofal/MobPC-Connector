package com.omar.pcconnector.network.api

import com.omar.pcconnector.network.connection.Connection
import kotlin.io.path.Path
import kotlin.io.path.name


fun getExternalDownloadURL(connection: Connection, filePath: String, accessToken: Token) =
     "http://${connection.ip}:${connection.port}/download/${Path(filePath).name}?path=$filePath&token=$accessToken"