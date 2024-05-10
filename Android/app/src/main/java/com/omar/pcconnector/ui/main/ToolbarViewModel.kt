package com.omar.pcconnector.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.network.connection.ConnectionStatus
import com.omar.pcconnector.operation.SimpleOperationManager
import com.omar.pcconnector.pcApi
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.event.ApplicationOperation
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ToolbarViewModel @AssistedInject constructor(
    @Assisted val connectionStatusFlow: StateFlow<ConnectionStatus>,
    @Assisted val serverName: String,
    private val appEventsFlow: MutableSharedFlow<ApplicationEvent>
) : ViewModel() {


    init {

        viewModelScope.launch {
            connectionStatusFlow.collect {
                operationManager = if (it is ConnectionStatus.Connected) {
                    SimpleOperationManager(it.connection.retrofit.pcApi())
                } else {
                    null
                }
            }
        }

    }


    private var operationManager: SimpleOperationManager? = null

    fun lockPC() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val operationManager = getOperationManagerOrShowError() ?: return@launch
                operationManager.lockPC()
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.LOCK_PC, true))
            } catch (e: Throwable) {
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.LOCK_PC, false))
            }
        }
    }


    fun shutdownPC() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val operationManager = getOperationManagerOrShowError() ?: return@launch
                operationManager.shutdownPC()
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.SHUTDOWN_PC, true))
            } catch (e: Throwable) {
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.SHUTDOWN_PC, false))
            }
        }
    }

    fun openLinkInBrowser(link: String, incognito: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val operationManager = getOperationManagerOrShowError() ?: return@launch
                operationManager.openInBrowser(link, incognito)
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.OPEN_URL, true))
            } catch (e: Throwable) {
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.OPEN_URL, false))
            }
        }
    }

    private fun getOperationManagerOrShowError(): SimpleOperationManager? {
        return when (val op = operationManager) {
            null -> {
                Log.e("VM", "No Connection currently")
                null
            }

            else -> op
        }
    }

    fun copyToPCClipboard(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val operationManager = getOperationManagerOrShowError() ?: return@launch
                operationManager.copyToPCClipboard(data)
                appEventsFlow.emit(
                    ApplicationEvent(
                        ApplicationOperation.COPY_TO_CLIPBOARD,
                        true
                    )
                )
            } catch (e: Throwable) {
                appEventsFlow.emit(
                    ApplicationEvent(
                        ApplicationOperation.COPY_TO_CLIPBOARD,
                        false
                    )
                )
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(connectionStatusFlow: StateFlow<ConnectionStatus>, serverName: String): ToolbarViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            connectionStatusFlow: StateFlow<ConnectionStatus>,
            serverName: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(connectionStatusFlow, serverName) as T
            }
        }
    }

}
