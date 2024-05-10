package com.omar.pcconnector.network.ws

import android.util.Log
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.network.connection.Connection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.nio.file.Path


/**
 * Registers with the server to watch a particular directory or file with changes
 * and emits events.
 */
class FileSystemWatcher(
    private val connection: Connection
): WebSocketListener() {

    private var socket: WebSocket? = null
    private var isSocketClosed = true

    private var currentWatchedPath: Path? = null

    private val okHttpClient = OkHttpClient()

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow: SharedFlow<Event>
        get() = _eventFlow

    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (currentWatchedPath == null) return
        webSocket.send(currentWatchedPath!!.absolutePath)
        Log.i(TAG, "Connection opened")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val scope = CoroutineScope(Dispatchers.Main)
        if (text == "changed") {
            scope.launch { _eventFlow.emit(Event.CHANGED) }
        } else if (text == "file deleted") {
            scope.launch { _eventFlow.emit(Event.DELETED) }
        }
        Log.i(TAG, "New message: $text")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        isSocketClosed = true
        Log.e(TAG, "Socket closed $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isSocketClosed = true
        Log.e(TAG, "Socket failed ${t.message}")
    }

    /**
     * Establish a connection with the server
     */
    private fun initConnection() {
        if (socket != null && !isSocketClosed) return
        val requestURL = "ws://${connection.ip}:6544/"
        val request = Request.Builder().url(requestURL).build()
        socket = okHttpClient.newWebSocket(request, this)
    }

    /**
     * Watch a path for changes. Note that this removes previously watched path if any
     */
    fun watch(path: Path) {
        currentWatchedPath = path
        if (socket != null)
            socket!!.send(path.absolutePath)
        else {
            isSocketClosed = false
            initConnection()
        }
    }

    fun stopWatching() {
        socket?.cancel()
        socket = null
        isSocketClosed = true
    }

    /**
     * Close the connection and free resources. Note that the class is not reusable again
     */
    fun close() {
        socket?.cancel()
        okHttpClient.dispatcher.executorService.shutdown()
        isSocketClosed = true
    }

    companion object {
        private const val TAG = "Watcher"
    }

    enum class Event {
        CHANGED, DELETED
    }
}