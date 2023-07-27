package com.omar.pcconnector.ui.main

import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.api.getDownloadURL
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.network.connection.ConnectionHeartbeat
import com.omar.pcconnector.network.ws.FileSystemWatcher
import com.omar.pcconnector.operation.CopyResourcesOperation
import com.omar.pcconnector.operation.DeleteOperation
import com.omar.pcconnector.operation.GetDrivesOperation
import com.omar.pcconnector.operation.ListDirectoryOperation
import com.omar.pcconnector.operation.MakeDirectoriesOperation
import com.omar.pcconnector.operation.RenameOperation
import com.omar.pcconnector.operation.transfer.TransfersManager
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.event.ApplicationOperation
import com.omar.pcconnector.ui.nav.ImageScreen
import com.omar.pcconnector.ui.nav.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.pathString


@Suppress("UNCHECKED_CAST")
class FileSystemViewModel @AssistedInject constructor(
    private val transfersManager: TransfersManager,
    private val eventsFlow: MutableSharedFlow<ApplicationEvent>,
    private val navigator: Navigator,
    @Assisted private val connection: Connection
) : ViewModel() {

    private val api = connection.retrofit.create(FileSystemOperations::class.java)

    private val _state: MutableStateFlow<FileSystemState> =
        MutableStateFlow(FileSystemState.Loading)

    val state: Flow<FileSystemState>
        get() = _state

    private var _copiedResource = MutableStateFlow<Path?>(null)
    val copiedResource: Flow<Path?>
        get() = _copiedResource


    private val watcher = FileSystemWatcher(connection)

    private val heartbeat = ConnectionHeartbeat(connection).apply { start() }
    private val serverStateFlow = heartbeat.state

    init {

        launchInitFunctions()

        viewModelScope.launch {
            watcher.eventFlow.collectLatest {
                refresh()
            }
        }

        viewModelScope.launch {
            serverStateFlow.collect { serverState ->
                val event = when (serverState) {
                    ConnectionHeartbeat.State.UNAVAILABLE -> ApplicationEvent(
                        ApplicationOperation.PING_SERVER,
                        false
                    )

                    ConnectionHeartbeat.State.AVAILABLE -> ApplicationEvent(
                        ApplicationOperation.PING_SERVER,
                        true
                    )

                    else -> null
                }
                event?.let { eventsFlow.emit(it) }
                if (serverState == ConnectionHeartbeat.State.UNAVAILABLE)
                    watcher.stopWatching()
                else {
                    if (state is FileSystemState.Initialized) {
                        watchCurrentDirectory()
                        refresh()
                    }
                }
            }
        }
    }

    private fun launchInitFunctions() {
        // 1) load drives
        // 2) when drives are loaded, get the home directory and load the path
        // Note we need to handle errors
        viewModelScope.launch {
            val drives = GetDrivesOperation(api).start()
            val directoryToLoad = Paths.get(drives.first(), "/")
            _state.value = FileSystemState.Initialized.Loading(directoryToLoad, drives, emptyList())
            loadDirectory(directoryToLoad)
        }
    }

    private fun loadDirectory(path: Path) {
        assert(_state.value !is FileSystemState.Loading)
        viewModelScope.launch {
            val state = _state.value as FileSystemState.Initialized
            _state.value = FileSystemState.Initialized.Loading(path, state.drives, state.directoryStructure)
            refresh()
            watchCurrentDirectory()
        }
    }

    private fun refresh() {
        assert(_state.value !is FileSystemState.Loading)
        viewModelScope.launch {
            val state = _state.value as FileSystemState.Initialized
            val currentPath = state.currentDirectory
            try {
                val directoryContents = ListDirectoryOperation(api, currentPath).start()
                _state.value =
                    FileSystemState.Initialized.Loaded(currentPath, state.drives, directoryContents)
            } catch (e: Exception) { // if for any reason we can't access the folder, just go to its parent
                loadDirectory(currentPath.parent)
            }

        }
    }

    fun onResourceClicked(resource: Resource) {
        if (resource is DirectoryResource) {
            loadDirectory(resource.path)
        } else if (resource.path.extension == "jpg") {
            navigator.navigate(ImageScreen.navigationCommand(resource.path.absolutePathString()))
        }
    }

    fun onPathChanged(path: Path) {
        loadDirectory(path)
    }

    fun onNavigateBack() {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Initialized
        if (state.currentDirectory.nameCount <= 1) return
        val currentPath = state.currentDirectory
        loadDirectory(currentPath.parent)
    }

    fun mkdirs(name: String) {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Initialized
        viewModelScope.launch(Dispatchers.IO) {
            try {
                MakeDirectoriesOperation(api, state.currentDirectory, name).start()
            } catch (e: NoSuchElementException) {
                Log.e("MKDIR", "COULD NOT MAKE DIRECTORY")
            }
        }
    }

    fun renameResource(resource: Resource, newName: String, overwrite: Boolean = false) {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Initialized
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filePath = resource.path
                RenameOperation(api, filePath, newName, overwrite).start()
                loadDirectory(state.currentDirectory)
            } catch (e: NoSuchElementException) {
                Log.e("RENAME", "COULD NOT RENAME")
            }
        }
    }

    fun deleteResource(resource: Resource) {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Initialized
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filePath = resource.path
                DeleteOperation(api, filePath, false).start()
                loadDirectory(state.currentDirectory)
            } catch (e: NoSuchElementException) {
                Log.e("RENAME", "COULD NOT RENAME")
            }
        }
    }

    fun pasteResource() {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Initialized
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_copiedResource.value == null) throw NoSuchElementException()
                val destPath = state.currentDirectory
                val srcPath = _copiedResource.value!!
                CopyResourcesOperation(api, srcPath, destPath, false).start()
                _copiedResource.value = null
            } catch (e: NoSuchElementException) {
                Log.e("RENAME", "COULD NOT RENAME")
            }
        }
    }

    private fun watchCurrentDirectory() {
        val state = _state.value
        if (state is FileSystemState.Initialized)
            watcher.watch(state.currentDirectory)
    }

    fun copyResource(resource: Resource) {
        _copiedResource.value = resource.path
    }

    fun download(
        resource: Resource,
        destinationFolder: DocumentFile
    ) {
        transfersManager.download(
            connection,
            resource.path,
            destinationFolder
        )
    }

    fun upload(
        documents: List<DocumentFile>
    ) {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Initialized
        transfersManager.upload(documents, connection, state.currentDirectory)
    }

    @AssistedFactory
    interface Factory {
        fun create(connection: Connection): FileSystemViewModel
    }

    companion object {
        fun provideFactory(
            factory: Factory,
            connection: Connection
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(connection) as T
            }
        }
    }

    override fun onCleared() {
        heartbeat.stop()
        watcher.close()
        super.onCleared()
    }

}


sealed class FileSystemState {

    object Loading : FileSystemState()


    sealed class Initialized(
        val currentDirectory: Path,
        val drives: List<String>,
        val directoryStructure: List<Resource>
    ) : FileSystemState() {

        class Loading(
            currentDirectory: Path,
            drives: List<String>,
           directoryStructure: List<Resource>
        ) : Initialized(currentDirectory, drives, directoryStructure)

        class Loaded(
            currentDirectory: Path,
            drives: List<String>,
            directoryStructure: List<Resource>
        ) : Initialized(currentDirectory, drives, directoryStructure)


    }


}

