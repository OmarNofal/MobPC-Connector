package com.omar.pcconnector.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.network.api.PCOperations
import com.omar.pcconnector.operation.CopyToClipboardOperation
import com.omar.pcconnector.operation.LockPCOperation
import com.omar.pcconnector.operation.OpenLinkOperation
import com.omar.pcconnector.operation.ShutdownPCOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject


@HiltViewModel
class ToolbarViewModel @Inject constructor(
    retrofit: Retrofit?,
): ViewModel() {

    private val api = retrofit?.create(PCOperations::class.java)
        ?: throw java.lang.IllegalArgumentException("Retrofit instance is null. Make sure the connection is initialized before constructing viewModel")

    val serverName = "Omar's Server"

    fun lockPC() {
        viewModelScope.launch(Dispatchers.IO) {
            LockPCOperation(api).start()
        }
    }


    fun shutdownPC() {
        viewModelScope.launch(Dispatchers.IO) {
            ShutdownPCOperation(api).start()
        }
    }

    fun openLinkInBrowser(link: String, incognito: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            OpenLinkOperation(api, link, incognito).start()
        }
    }

    fun copyToPCClipboard(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CopyToClipboardOperation(api, data).start()
        }
    }


}
