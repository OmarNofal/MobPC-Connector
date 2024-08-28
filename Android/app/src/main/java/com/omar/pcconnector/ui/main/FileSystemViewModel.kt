package com.omar.pcconnector.ui.main

import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.fileSystemApi
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.FileResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.getExternalDownloadURL
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.network.connection.ConnectionStatus
import com.omar.pcconnector.network.ws.FileSystemWatcher
import com.omar.pcconnector.operation.CopyResourcesOperation
import com.omar.pcconnector.operation.DeleteOperation
import com.omar.pcconnector.operation.GetDrivesOperation
import com.omar.pcconnector.operation.GetFileAccessToken
import com.omar.pcconnector.operation.ListDirectoryOperation
import com.omar.pcconnector.operation.MakeDirectoriesOperation
import com.omar.pcconnector.operation.RenameOperation
import com.omar.pcconnector.operation.transfer.TransfersManager
import com.omar.pcconnector.preferences.ServerPreferences.FileSystemSortCriteria
import com.omar.pcconnector.preferences.ServerPreferences.FoldersAndFilesSeparation.DEFAULT
import com.omar.pcconnector.preferences.ServerPreferences.FoldersAndFilesSeparation.FILES_FIRST
import com.omar.pcconnector.preferences.ServerPreferences.FoldersAndFilesSeparation.FOLDERS_FIRST
import com.omar.pcconnector.preferences.UserPreferencesRepository
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.event.ApplicationOperation
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.Paths


@Suppress("UNCHECKED_CAST")
class FileSystemViewModel @AssistedInject constructor(
    private val transfersManager: TransfersManager,
    private val eventsFlow: MutableSharedFlow<ApplicationEvent>,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Assisted private val connectionStatusFlow: StateFlow<ConnectionStatus>,
    @Assisted private val serverId: String,
    @Assisted("token") private val token: String,
) : ViewModel() {


    private val _state: MutableStateFlow<FileSystemState> =
        MutableStateFlow(FileSystemState.Loading)

    val state: Flow<FileSystemState>
        get() = _state

    private var _copiedResource = MutableStateFlow<Path?>(null)
    val copiedResource: Flow<Path?>
        get() = _copiedResource


    private lateinit var watcher: FileSystemWatcher

    init {

        launchInitFunctions()

    }

    private fun launchInitFunctions() {
        // 1) load drives
        // 2) when drives are loaded, get the home directory and load the path
        // Note we need to handle errors
        viewModelScope.launch {
            connectionStatusFlow.transformWhile {
                if (it is ConnectionStatus.Connected) {
                    emit(it)

                    watcher = FileSystemWatcher(it.connection)
                    viewModelScope.launch {
                        watcher.eventFlow.collectLatest(::onWatcherNotification)
                    }

                    false
                } else {
                    true
                }
            }.collect {
                val api = it.connection.retrofit.fileSystemApi()
                val drives = GetDrivesOperation(api).start()
                val directoryToLoad =
                    userPreferencesRepository.getServerPreferences(serverId).startPath?.let(
                        Paths::get
                    ) ?: Paths.get(drives.first(), "/")
                _state.value = FileSystemState.Loaded(
                    INVALID_PATH, drives, emptyList(), directoryToLoad
                )
                loadDirectory(directoryToLoad)
            }
        }

        viewModelScope.launch {
            var previousState = connectionStatusFlow.value
            connectionStatusFlow.collect {
                if (it is ConnectionStatus.Connected) {
                    eventsFlow.emit(
                        ApplicationEvent(
                            ApplicationOperation.PING_SERVER, true
                        )
                    )
                } else if ((it is ConnectionStatus.NotFound) && (previousState is ConnectionStatus.Connected)) {
                    eventsFlow.emit(
                        ApplicationEvent(
                            ApplicationOperation.PING_SERVER, false
                        )
                    )
                }
                previousState = it
            }
        }

    }

    private fun loadDirectory(path: Path) {
        assert(_state.value !is FileSystemState.Loading)
        viewModelScope.launch {

            onDirectoryLoading(path)
            val directoryContents =
                getDirectoryContents(path).let(::performDirectoryTransformations)
            onDirectoryLoaded(
                path,
                directoryContents
            )

            watchDirectory(path)
        }
    }

    /**
     * Performs transformations chosen by the user
     * in the server settings
     */
    private fun performDirectoryTransformations(resources: List<Resource>): List<Resource> {

        val serverPreferences =
            userPreferencesRepository.getServerPreferences(serverId)

        return resources
            .filter {
                serverPreferences.showHiddenResources || !it.name.startsWith(".")
            }.sortedWith { o1, o2 ->
                when (serverPreferences.sortingCriteria) {
                    FileSystemSortCriteria.NAME -> o1.name.compareTo(
                        o2.name,
                        ignoreCase = true
                    )

                    FileSystemSortCriteria.SIZE -> o1.size.compareTo(o2.size)
                    FileSystemSortCriteria.MODIFICATION_DATE -> o1.modificationDateMs.compareTo(
                        o2.modificationDateMs
                    )

                    else -> 0
                }
            }.sortedWith { o1, o2 ->
                if (o1 is DirectoryResource && o2 is DirectoryResource) return@sortedWith 0
                if (o1 is FileResource && o2 is FileResource) return@sortedWith 0
                val separation = serverPreferences.foldersAndFilesSeparation
                when (separation) {
                    DEFAULT -> -1
                    FOLDERS_FIRST -> if (o1 is DirectoryResource) -1 else 1
                    FILES_FIRST -> if (o1 is FileResource) -1 else 1
                    else -> 0
                }
            }
    }


    private fun onDirectoryLoading(path: Path) {

        val state = _state.value as FileSystemState.Loaded

        _state.value = FileSystemState.Loaded(
            state.currentDirectory, state.drives, state.directoryStructure, path
        )
    }

    private fun onDirectoryLoaded(
        path: Path, directoryContents: List<Resource>
    ) {

        assert(_state.value is FileSystemState.Loaded)

        val oldState = _state.value as FileSystemState.Loaded

        _state.value = FileSystemState.Loaded(
            path, oldState.drives, directoryContents, null
        )
    }

    private suspend fun getDirectoryContents(path: Path): List<Resource> {
        return try {
            val api = getConnectionOrShowError()?.retrofit?.fileSystemApi()
                ?: return emptyList()

            ListDirectoryOperation(api, path).start()
        } catch (e: Exception) { // if for any reason we can't access the folder, just go to its parent
            emptyList()
        }
    }

    private val _openFileEvents = MutableSharedFlow<Pair<String, String>>()
    val openFileEvents: SharedFlow<Pair<String, String>>
        get() = _openFileEvents

    fun onResourceClicked(resource: Resource) {
        if (resource is DirectoryResource) {
            loadDirectory(resource.path)
        } else {
            viewModelScope.launch {
                val connection = getConnectionOrShowError() ?: return@launch
                val accessToken = try {
                    GetFileAccessToken(
                        connection.retrofit.fileSystemApi(), resource.path
                    ).start()
                } catch (e: Exception) {
                    ""
                }
                val fileDownloadURL = getExternalDownloadURL(
                    connection, resource.path.absolutePath, accessToken
                )
                Log.i("OPEN_FILE", fileDownloadURL)
                viewModelScope.launch {
                    _openFileEvents.emit(fileDownloadURL to resource.name)
                }
            }
        }
    }

    private fun onWatcherNotification(event: FileSystemWatcher.Event) {
        val currentState = _state.value as? FileSystemState.Loaded ?: return
        loadDirectory(currentState.currentDirectory)
    }

    fun onPathChanged(path: Path) {
        loadDirectory(path)
    }

    fun onNavigateBack() {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Loaded
        if (state.currentDirectory.nameCount <= 1) return
        val currentPath = state.currentDirectory
        loadDirectory(currentPath.parent)
    }

    fun mkdirs(name: String) {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Loaded
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = getConnectionOrShowError()?.retrofit?.fileSystemApi()
                    ?: return@launch
                MakeDirectoriesOperation(
                    api, state.currentDirectory, name
                ).start()
            } catch (e: NoSuchElementException) {
                Log.e("MKDIR", "COULD NOT MAKE DIRECTORY")
            }
        }
    }

    fun renameResource(
        resource: Resource, newName: String, overwrite: Boolean = false
    ) {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Loaded
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = getConnectionOrShowError()?.retrofit?.fileSystemApi()
                    ?: return@launch
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
        val state = _state.value as FileSystemState.Loaded
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = getConnectionOrShowError()?.retrofit?.fileSystemApi()
                    ?: return@launch
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
        val state = _state.value as FileSystemState.Loaded

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_copiedResource.value == null) throw NoSuchElementException()
                val api = getConnectionOrShowError()?.retrofit?.fileSystemApi()
                    ?: return@launch
                val destPath = state.currentDirectory
                val srcPath = _copiedResource.value!!
                CopyResourcesOperation(api, srcPath, destPath, false).start()
                _copiedResource.value = null
            } catch (e: NoSuchElementException) {
                Log.e("RENAME", "COULD NOT RENAME")
            }
        }
    }

    private fun watchDirectory(path: Path) {
        if (!::watcher.isInitialized) return
        watcher.watch(path)
    }

    fun copyResource(resource: Resource) {
        _copiedResource.value = resource.path
    }

    fun download(
        resource: Resource, destinationFolder: DocumentFile
    ) {
        transfersManager.download(
            serverId, resource.path, destinationFolder
        )
    }

    fun upload(
        documents: List<DocumentFile>
    ) {
        assert(_state.value !is FileSystemState.Loading)
        val state = _state.value as FileSystemState.Loaded
        transfersManager.upload(documents, serverId, state.currentDirectory)
    }

    /**
     * Get the connection if we are currently connected or return null and log error
     */
    private fun getConnectionOrShowError(): Connection? {
        return when (val s = connectionStatusFlow.value) {
            is ConnectionStatus.Connected -> s.connection
            else -> {
                Log.e("VM", "No Connection currently")
                null
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            connectionStatusFlow: StateFlow<ConnectionStatus>,
            serverId: String,
            @Assisted("token") token: String
        ): FileSystemViewModel
    }

    companion object {
        fun provideFactory(
            factory: Factory,
            connectionStatusFlow: StateFlow<ConnectionStatus>,
            serverId: String,
            token: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(
                    connectionStatusFlow, serverId, token
                ) as T
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        watcher.close()
    }

}


sealed class FileSystemState {

    data object Loading : FileSystemState()

    data class Loaded(
        val currentDirectory: Path,
        val drives: List<String>,
        val directoryStructure: List<Resource>,
        val currentlyLoadingDirectory: Path?
    ) : FileSystemState()

}

val INVALID_PATH: Path = Paths.get("")