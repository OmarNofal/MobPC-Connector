package com.omar.pcconnector.operation

import com.omar.pcconnector.network.api.PCOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SimpleOperationManager(
    private val pcOperationsAPI: PCOperations
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun lockPC() {
        scope.async {
            LockPCOperation(pcOperationsAPI).start()
        }.await()
    }

    suspend fun shutdownPC() {
        scope.async {
            ShutdownPCOperation(pcOperationsAPI).start()
        }.await()
    }


    suspend fun copyToPCClipboard(data: String) {
        scope.async {
            CopyToClipboardOperation(pcOperationsAPI, data).start()
        }.await()
    }

    suspend fun openInBrowser(url: String, incognito: Boolean) {
        scope.async {
            OpenLinkOperation(pcOperationsAPI, url, incognito).start()
        }.await()
    }


}