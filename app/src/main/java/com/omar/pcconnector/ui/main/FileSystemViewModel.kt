package com.omar.pcconnector.ui.main

import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.operation.CopyResourcesOperation
import com.omar.pcconnector.operation.DeleteOperation
import com.omar.pcconnector.operation.ListDirectoryOperation
import com.omar.pcconnector.operation.MakeDirectoriesOperation
import com.omar.pcconnector.operation.RenameOperation
import com.omar.pcconnector.operation.transfer.TransfersManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Stack



@Suppress("UNCHECKED_CAST")
class FileSystemViewModel @AssistedInject constructor(
    private val transfersManager: TransfersManager,
    @Assisted private val connection: Connection
) : ViewModel() {

    private val api = connection.retrofit.create(FileSystemOperations::class.java)

    private val _state: MutableStateFlow<FileSystemState> =
        MutableStateFlow(FileSystemState.NormalState(Paths.get("~"), listOf(), true))
    val state: Flow<FileSystemState>
        get() = _state

    private var _copiedResource = MutableStateFlow<Path?>(null)
    val copiedResource: Flow<Path?>
        get() = _copiedResource

    private val navigationBackstack: Stack<FileSystemState> = Stack()

    init {
        loadDirectory(Paths.get("~"), false)
    }

    private fun loadDirectory(path: Path, addToBackstack: Boolean = true) {
        viewModelScope.launch {
            if (addToBackstack) {
                navigationBackstack.push(_state.value)
            }
            _state.value = FileSystemState.NormalState(
                _state.value.currentDirectory, _state.value.directoryStructure, true
            )
            Log.i("LIST_DIR", "Started loading directory")
            val directoryContents = ListDirectoryOperation(api, path).start()
            Log.i("LIST_DIR", "DONE Listing Dir")
            _state.value = FileSystemState.NormalState(
                path, directoryContents, false
            )
        }
    }

    fun onResourceClicked(resource: Resource) {
        if (resource is DirectoryResource) {
            val currentPath = _state.value.currentDirectory
            loadDirectory(currentPath.resolve(resource.name))
        }
    }

    fun onNavigateBack() {
        if (navigationBackstack.empty()) return
        _state.value = navigationBackstack.pop()
    }

    fun mkdirs(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                MakeDirectoriesOperation(api, _state.value.currentDirectory, name).start()
                loadDirectory(_state.value.currentDirectory, false)
            } catch (e: NoSuchElementException) {
                Log.e("MKDIR", "COULD NOT MAKE DIRECTORY")
            }
        }
    }

    fun renameResource(resource: Resource, newName: String, overwrite: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filePath = _state.value.currentDirectory.resolve(resource.name)
                RenameOperation(api, filePath, newName, overwrite).start()
                loadDirectory(_state.value.currentDirectory, false)
            } catch (e: NoSuchElementException) {
                Log.e("RENAME", "COULD NOT RENAME")
            }
        }
    }

    fun deleteResource(resource: Resource) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filePath = _state.value.currentDirectory.resolve(resource.name)
                DeleteOperation(api, filePath, false).start()
                loadDirectory(_state.value.currentDirectory, false)
            } catch (e: NoSuchElementException) {
                Log.e("RENAME", "COULD NOT RENAME")
            }
        }
    }

    fun pasteResource() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_copiedResource.value == null) throw NoSuchElementException()
                val destPath = _state.value.currentDirectory
                val srcPath = _copiedResource.value!!
                CopyResourcesOperation(api, srcPath, destPath, false).start()
                _copiedResource.value = null
            } catch (e: NoSuchElementException) {
                Log.e("RENAME", "COULD NOT RENAME")
            }
        }
    }

    fun copyResource(resource: Resource) {
        _copiedResource.value = _state.value.currentDirectory.resolve(resource.name)
    }

    fun download(
        resource: Resource,
        destinationFolder: DocumentFile
    ) {
        transfersManager.download(
            connection,
            _state.value.currentDirectory.resolve(resource.name),
            destinationFolder
        )
    }

    fun upload(
        documents: List<DocumentFile>
    ) {
        transfersManager.upload(documents, connection, _state.value.currentDirectory)
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


}


sealed class FileSystemState(
    val currentDirectory: Path,
    val directoryStructure: List<Resource>,
    val isLoading: Boolean
) {

    class NormalState(
        currentDirectory: Path,
        directoryStructure: List<Resource>,
        isLoading: Boolean
    ) : FileSystemState(currentDirectory, directoryStructure, isLoading)

    class Loading: FileSystemState(Paths.get(""), listOf(), true)

}

