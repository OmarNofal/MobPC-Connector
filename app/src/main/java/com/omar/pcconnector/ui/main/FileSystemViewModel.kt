package com.omar.pcconnector.ui.main

import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.operation.DeleteOperation
import com.omar.pcconnector.operation.ListDirectoryOperation
import com.omar.pcconnector.operation.MakeDirectoriesOperation
import com.omar.pcconnector.operation.RenameOperation
import com.omar.pcconnector.operation.transfer.TransfersManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Stack
import javax.inject.Inject


@HiltViewModel
class FileSystemViewModel @Inject constructor(
    private val connection: Connection?,
    private val transfersManager: TransfersManager
) : ViewModel() {

    private val api = connection?.retrofit?.create(FileSystemOperations::class.java)
        ?: throw java.lang.IllegalArgumentException("")

    private val _state: MutableStateFlow<FileSystemState> =
        MutableStateFlow(FileSystemState.NormalState(Paths.get("~"), listOf(), true))
    val state: Flow<FileSystemState>
        get() = _state

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

    fun download(
        resource: Resource,
        destinationFolder: DocumentFile
    ) {
        transfersManager.download(
            connection!!,
            _state.value.currentDirectory.resolve(resource.name),
            destinationFolder
        )
    }

    fun upload(
        documents: List<DocumentFile>
    ) {
        transfersManager.upload(documents, connection!!, _state.value.currentDirectory)
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

