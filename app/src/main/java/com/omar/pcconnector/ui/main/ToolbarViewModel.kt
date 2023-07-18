package com.omar.pcconnector.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.network.api.PCOperations
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.operation.SimpleOperationManager
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.event.ApplicationOperation
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


class ToolbarViewModel @AssistedInject constructor(
    @Assisted val connection: Connection,
    private val appEventsFlow: MutableSharedFlow<ApplicationEvent>
): ViewModel() {

    val serverName: String = connection.serverName

    private val operationManager: SimpleOperationManager
        = SimpleOperationManager(
        connection.retrofit.create(PCOperations::class.java)
        )

    fun lockPC() {
        viewModelScope.launch(Dispatchers.IO) {
            var success = true
            try {
                operationManager.lockPC()
            } catch (e: Throwable) {
                success = false
            } finally {
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.LOCK_PC, success))
            }
        }
    }


    fun shutdownPC() {
        viewModelScope.launch(Dispatchers.IO) {
            var success = true
            try {
                operationManager.shutdownPC()
            } catch (e: Throwable) {
                success = false
            } finally {
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.SHUTDOWN_PC, success))
            }
        }
    }

    fun openLinkInBrowser(link: String, incognito: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            var success = true
            try {
                operationManager.openInBrowser(link, incognito)
            } catch (e: Throwable) {
                success = false
            } finally {
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.OPEN_URL, success))
            }
        }
    }

    fun copyToPCClipboard(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var success = true
            try {
                operationManager.copyToPCClipboard(data)
            } catch (e: Throwable) {
                success = false
            } finally {
                appEventsFlow.emit(ApplicationEvent(ApplicationOperation.COPY_TO_CLIPBOARD, success))
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(connection: Connection): ToolbarViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            connection: Connection
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(connection) as T
            }
        }
    }

}
