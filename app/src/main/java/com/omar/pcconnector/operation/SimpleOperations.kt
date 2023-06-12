package com.omar.pcconnector.operation

import com.omar.pcconnector.network.api.PCOperations
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


