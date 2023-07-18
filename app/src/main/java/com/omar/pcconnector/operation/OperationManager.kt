package com.omar.pcconnector.operation

import com.omar.pcconnector.network.api.PCOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SimpleOperationManager(
    private val pcOperationsAPI: PCOperations
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun lockPC() {
        scope.launch {
            LockPCOperation(pcOperationsAPI).start()
        }.join()
    }

    suspend fun shutdownPC() {
        scope.launch {
            ShutdownPCOperation(pcOperationsAPI).start()
        }.join()
    }


    suspend fun copyToPCClipboard(data: String) {
        withContext(scope.coroutineContext) {
            delay(1000)
            CopyToClipboardOperation(pcOperationsAPI, data).start()
        }
    }

    suspend fun openInBrowser(url: String, incognito: Boolean) {
        scope.launch {
            OpenLinkOperation(pcOperationsAPI, url, incognito).start()
        }.join()
    }


}