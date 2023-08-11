package com.omar.pcconnector.ui.preview

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.fileSystemApi
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.operation.transfer.TransfersManager
import com.omar.pcconnector.ui.nav.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.io.IOException
import java.nio.file.Path


class ImagePreviewViewModel @AssistedInject constructor(
    private val transfersManager: TransfersManager,
    private val navigator: Navigator,
    @Assisted private val retrofit: Retrofit,
    @Assisted private val imagePath: Path,
) : ViewModel() {

    private val _state = MutableStateFlow<ImagePreviewState>(ImagePreviewState.Downloading)
    val state: Flow<ImagePreviewState>
        get() = _state

    private val _events = MutableSharedFlow<ImagePreviewEvents>()
    val events: SharedFlow<ImagePreviewEvents>
        get() = _events

    val imageName = imagePath.fileName.toString()

    private var shareFlag = false // whether the user intends to share
    private var downloadDirectory: DocumentFile? = null

    init {
        downloadImageTempFile()
    }

    private fun downloadImageTempFile() {
        viewModelScope.launch {
            try {
                val fileUri =
                    transfersManager.downloadTemporaryFile(retrofit.fileSystemApi(), imagePath)
                setReadyState(fileUri)
                trySharing()
            } catch (e: IOException) {
                setErrorState(null, ImagePreviewErrors.NETWORK_EXCEPTION)
            } catch (e: Exception) {
                setErrorState(null, ImagePreviewErrors.UNKNOWN_EXCEPTION)
            }
        }
    }

    private fun setReadyState(uri: Uri) {
        _state.value = ImagePreviewState.Downloaded.Ready(uri)
    }

    private fun setErrorState(fileUri: Uri?, error: ImagePreviewErrors) {
        if (fileUri == null) {
            _state.value = ImagePreviewState.Error(error)
        } else {
            _state.value = ImagePreviewState.Downloaded.RenderError(fileUri, error)
        }
    }

    fun onDownloadFile(directory: DocumentFile) {
        downloadDirectory = directory
        tryDownloading()
    }

    fun onRetry() {
        downloadImageTempFile()
    }

    fun onShare() {
        shareFlag = true
        trySharing()
    }

    private fun trySharing() {
        if (!shareFlag) return
        val state = (_state.value as? ImagePreviewState.Downloaded) ?: return
        viewModelScope.launch {
            _events.emit(ImagePreviewEvents.ShareEvent(state.uri))
            shareFlag = false
        }
    }

    private fun tryDownloading() {
        val directory = downloadDirectory ?: return
        val state = (_state.value as? ImagePreviewState.Downloaded) ?: return
        viewModelScope.launch {
            _events.emit(ImagePreviewEvents.DownloadingEvent)
            downloadDirectory = null
            val file = directory.createFile("no_mime_type", imageName) ?: return@launch
            transfersManager.copyFile(state.uri, file)
            _events.emit(ImagePreviewEvents.DownloadedEvent)
        }
    }

    fun onRenderError() {
        val state = (_state.value as? ImagePreviewState.Downloaded) ?: return
        setErrorState(state.uri, ImagePreviewErrors.RENDER_ERROR)
    }

    fun closeScreen() {
        navigator.goBack()
    }

    @AssistedFactory
    interface Factory {
        fun create(retrofit: Retrofit, imagePath: Path): ImagePreviewViewModel
    }

    companion object {
        fun provideFactory(
            factory: Factory,
            retrofit: Retrofit,
            imagePath: Path,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(retrofit, imagePath) as T
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val state = (_state.value as? ImagePreviewState.Downloaded) ?: return
        try {
            state.uri.toFile().delete()
        } catch (e: IOException) {
            Log.e("FILE_DELETE", "Failed to delete temp file")
        }
    }

}


sealed class ImagePreviewState {

    class Error(val errorType: ImagePreviewErrors) : ImagePreviewState()
    object Downloading : ImagePreviewState()

    sealed class Downloaded(val uri: Uri) : ImagePreviewState() {
        class Ready(uri: Uri) : Downloaded(uri)
        class RenderError(
            uri: Uri,
            val error: ImagePreviewErrors = ImagePreviewErrors.RENDER_ERROR
        ) :
            Downloaded(uri)
    }
}

sealed class ImagePreviewEvents {
    class ShareEvent(val uri: Uri) : ImagePreviewEvents()
    object DownloadedEvent : ImagePreviewEvents()
    object DownloadingEvent : ImagePreviewEvents()
}

enum class ImagePreviewErrors {
    NETWORK_EXCEPTION,
    RENDER_ERROR,
    UNKNOWN_EXCEPTION
}
