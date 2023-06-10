package com.omar.pcconnector.operation.transfer.upload

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import com.omar.pcconnector.absolutePath
import com.omar.pcconnector.network.api.FileSystemOperations
import com.omar.pcconnector.network.api.getDataOrThrow
import com.omar.pcconnector.operation.MonitoredOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.nio.file.Path
import java.util.concurrent.CancellationException
import kotlin.io.path.absolutePathString


sealed class UploadOperationState(
    val filesNames: List<String>
) {
    sealed class Initialized(
        filesNames: List<String>,
    ): UploadOperationState(filesNames) {

        class Uploading(
            filesNames: List<String>,
            val currentlyUploadingFile: String,
            val totalSize: Long,
            val uploadedSize: Long
        ): Initialized(filesNames)

    }

    class Initializing(
        filesNames: List<String>
    ): UploadOperationState(filesNames)
    class Failed(filesNames: List<String>): UploadOperationState(filesNames)
}


private typealias UploadRequest = Pair<MultipartBody.Part, Flow<Pair<String, Float>>>
private typealias UploadRequests = List<UploadRequest>

class UploadOperation(
    private val uploadApi: FileSystemOperations,
    private val documents: List<DocumentFile>,
    private val contentResolver: ContentResolver,
    private val uploadPath: Path
): MonitoredOperation<UploadOperationState, Unit>() {

    override val name: String
        get() = "Upload Files"
    override val operationDescription: String
        get() = "Uploading ${documents.size} files"

    override suspend fun start() {
        if (documents.isEmpty()) throw IllegalArgumentException()
        if (documents.size > 1 && documents.any {it.isDirectory}) throw Exception("You can upload either 1 directory or multiple files")
        upload()
    }

    private suspend fun upload() = withContext(Dispatchers.IO){


        val fileRequests = documents.flatMap { if (it.isFile) listOf(getFileRequestBody(it, listOf("."))) else getDirectoryRequestBodies(it) }
        val totalSize = fileRequests.sumOf { it.first.body().contentLength() }

        val parts = fileRequests.map { it.first }
        val progresses = fileRequests.map { it.second }
            .merge()

        val collectionJob = launch {
            progresses.collect {
                _progress.value = UploadOperationState.Initialized.Uploading(
                    listOf(""),
                    it.first, totalSize, (it.second * totalSize).toLong()
                )
            }
        }

        val destinationPart = MultipartBody.Part.createFormData(
            "dest", null,
            RequestBody.create(MediaType.get("text/plain"), uploadPath.absolutePath)
        )
        uploadApi.upload(
            parts.toMutableList().apply { add(destinationPart) }
        ).execute().body().getDataOrThrow()

        collectionJob.cancel()
    }


    private fun getFileRequestBody(file: DocumentFile, folders: List<String>): UploadRequest {
        val progressFlow = MutableStateFlow(-1.0f)
        val requestBody = UploadRequestBody(file.uri, contentResolver, progressFlow)
        return  MultipartBody.Part.createFormData(folders.joinToString("/").ifEmpty { "." }, file.name ?: "Unknown", requestBody) to
                progressFlow.map { progress -> (file.name ?: "Unknown") to progress }
    }

    private fun getDirectoryRequestBodies(directory: DocumentFile, folders: List<String> = listOf()): UploadRequests
    {
        val newFolders = mutableListOf<String>().apply { addAll(folders); add(directory.name ?: "Unknown Directory") }
        val requestBodies = mutableListOf<UploadRequest>()
        directory.listFiles()
            .forEach {
                if (it.isDirectory) requestBodies.addAll(getDirectoryRequestBodies(it, newFolders))
                else requestBodies.add(getFileRequestBody(it, newFolders))
            }
        return requestBodies
    }

    override suspend fun cancel() {
        TODO("Not yet implemented")
    }

    private val _progress = MutableStateFlow<UploadOperationState>(
        UploadOperationState.Initializing(
            listOf()
        )
    )

    override val progress: StateFlow<UploadOperationState>
        get() = _progress
}