package com.omar.pcconnector.operation

import com.google.gson.JsonParser
import com.omar.pcconnector.network.api.PCOperations
import com.omar.pcconnector.network.api.StatusAPI
import com.omar.pcconnector.network.api.getDataOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LockPCOperation(
    val api: PCOperations
): Operation<Unit>() {

    override val name: String
        get() = "Lock PC"

    override val operationDescription: String
        get() = "Locking the PC"

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            api.lockPC().execute().body().getDataOrThrow()
        }
    }

}



class CopyToClipboardOperation(
    val api: PCOperations,
    val data: String
): Operation<Unit>() {

    override val name: String
        get() = "Copy to clipboard"
    override val operationDescription: String
        get() = "Copying data to clipboard"

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            api.copyToClipboard(data).execute().body().getDataOrThrow()
        }
    }

}


class ShutdownPCOperation(
    val api: PCOperations
): Operation<Unit>() {

    override val name: String
        get() = "Shutdown"

    override val operationDescription: String
        get() = "Shutting down the PC"

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            api.shutdownPC().execute().body().getDataOrThrow()
        }
    }

}


class OpenLinkOperation(
    private val api: PCOperations,
    private val url: String,
    private val incognito: Boolean
): Operation<Unit>() {

    override val name: String
        get() = "Open URL"

    override val operationDescription: String
        get() = "Opening URL in the Browser"

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            api.openLink(url, if (incognito) 1 else 0).execute().body().getDataOrThrow()
        }
    }

}


class PingOperation(
    val api: StatusAPI
): Operation<Boolean>() {

    override val name: String
        get() = "Ping"

    override val operationDescription: String
        get() = "Pinging the Server"

    override suspend fun start(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                api.status()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

}


data class HostInfo(
    val name: String,
    val uuid: String,
    val os: String
)

class StatusOperation(
    val api: StatusAPI
): Operation<HostInfo>() {

    override val name: String
        get() = "Status Operation"
    override val operationDescription: String
        get() = TODO("Not yet implemented")

    override suspend fun start(): HostInfo {
        val bodyString = api.status().string()
        val json = JsonParser.parseString(bodyString).asJsonObject
        val name = json["name"].asString ?: throw IllegalStateException()
        val os = json["os"].asString ?: throw IllegalStateException()
        val id = json["id"].asString ?: throw IllegalStateException()
        return HostInfo(name, id, os)
    }
}