package com.omar.pcconnector.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.AutoCloseInputStream
import android.os.ParcelFileDescriptor.AutoCloseOutputStream
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.system.OsConstants
import android.util.Log
import android.webkit.MimeTypeMap
import com.omar.pcconnector.R
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.fileSystemApi
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.FileResource
import com.omar.pcconnector.model.PairedDevice
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.network.connection.Connectivity
import com.omar.pcconnector.operation.GetDrivesOperation
import com.omar.pcconnector.operation.GetResourceInfoOperation
import com.omar.pcconnector.operation.ListDirectoryOperation
import com.omar.pcconnector.operation.transfer.download.StreamFileOperation
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.UnsupportedOperationException
import java.nio.file.Paths
import kotlin.io.path.extension


/**
 * Implementation of [DocumentsProvider] that allows
 * the user to access the files on his computer
 * through the Storage Access Framework
 */
class PCDocumentProvider : DocumentsProvider() {


    /**
     * A map containing a cache of all connections
     * to speed up queries saving time on finding the device
     */
    private val connectionsCache = mutableMapOf<String, Connection>()

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(): Boolean = true


    override fun queryRoots(projection: Array<out String>?): Cursor {

        val proj = projection ?: DEFAULT_ROOT_PROJECTION
        val result = MatrixCursor(proj)

        val devicesRepository = getDevicesRepository()

        val devices = runBlocking { devicesRepository.getAllPairedDevices() }

        devices.forEach { pairedDevice ->
            addDeviceToCursor(pairedDevice, result)
        }

        return result
    }


    override fun queryDocument(
        documentId: String?,
        projection: Array<out String>?
    ): Cursor {

        Log.d("QUERY_DOC", documentId.toString())

        val proj = projection ?: DEFAULT_DOCUMENT_PROJECTION
        val result = MatrixCursor(proj)

        if (documentId == null) return result

        val (serverId, path) = splitDocumentId(documentId)

        if (path.isBlank())
        {
            addServerRootToCursor(serverId, documentId, result)
            return result
        }

        val connection = getDeviceConnection(serverId) ?: return result

        try {
            val resource = runBlocking {
                GetResourceInfoOperation(
                    connection.retrofit.fileSystemApi(),
                    Paths.get(path)
                ).start()
            }
            addResourceToCursor(resource, serverId, result)
        } catch (e: Exception) {
            Log.e("QUERY_DOCUMENT", e.stackTraceToString() + " " + documentId)
            addServerRootToCursor(serverId, documentId, result)
        }

        return result
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {

        val proj = projection ?: DEFAULT_DOCUMENT_PROJECTION
        val result = MatrixCursor(proj)

        Log.d("CHILD_DOCS", parentDocumentId.toString())

        if (parentDocumentId == null) return result

        val (serverId, directory) = splitDocumentId(parentDocumentId)
        val connection = getDeviceConnection(serverId) ?: return result

        if (directory.isBlank()) {
            // we are querying for drives
            val drives =
                runBlocking { GetDrivesOperation(connection.retrofit.fileSystemApi()).start() }
            drives.forEach { drive ->
                addDriveToCursor(drive, serverId, result)
            }

        } else {

            val resources = runBlocking {
                ListDirectoryOperation(
                    connection.retrofit.fileSystemApi(),
                    Paths.get(directory)
                ).start()
            }

            resources.forEach {
                addResourceToCursor(it, serverId, result)
            }
        }

        return result
    }


    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {

        Log.d("OPEN_DOC", documentId.toString())

        if (documentId == null) throw IllegalArgumentException("DocID null")

        val (s1, s2) = ParcelFileDescriptor.createReliableSocketPair()

        val (serverId, filePath) = splitDocumentId(documentId)
        Log.d("OPEN_DOC", filePath.toString())
        val connection = getDeviceConnection(serverId) ?: throw IOException("Could not find the server")

        scope.launch(Dispatchers.IO) {
            try {
                val oStream = ParcelFileDescriptor.AutoCloseOutputStream(s2)
                val stream = StreamFileOperation(connection.retrofit.fileSystemApi(), Paths.get(filePath)).start()
                try {
                    stream.copyTo(oStream)
                } finally {
                    stream.close()
                    oStream.close()
                }
            } catch (e: IOException) {
                Log.e("OPEN_DOC", "Error while streaming the file", e)
                try {
                    s2.close()
                } catch (closeException: IOException) {
                    Log.e("OPEN_DOC", "Error while closing s2", closeException)
                }
            }
        }

        return s1
    }

    private fun addResourceToCursor(
        resource: Resource,
        serverId: String,
        result: MatrixCursor
    ) {
        result.newRow().apply {
            add(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                serverId + DOCUMENT_ID_SEPARATOR + resource.path
            )
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, resource.name)
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)

            val mimeType =
                if (resource is DirectoryResource) DocumentsContract.Document.MIME_TYPE_DIR
                else MimeTypeMap.getSingleton().getMimeTypeFromExtension((resource as FileResource).path.extension) ?: "text/plain"

            add(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                mimeType
            )

            add(DocumentsContract.Document.COLUMN_SIZE, if (resource.size == 0.toLong()) null else resource.size)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, resource.modificationDateMs)
        }
    }

    /**
     * Fallback to add dummy data to cursor
     * when the operation fails or when asked for information
     * about the root
     */
    private fun addServerRootToCursor(
        serverId: String,
        documentId: String?,
        result: MatrixCursor
    ) {

        val devicesRepository = getDevicesRepository()
        val pairedDevice = runBlocking {  devicesRepository.getPairedDevice(serverId) }

        result.newRow().apply {
            add(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                documentId
            )
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, pairedDevice.deviceInfo.name)
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)

            add(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.MIME_TYPE_DIR
            )

            add(DocumentsContract.Document.COLUMN_SIZE, null)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null)
        }
    }

    private fun addDriveToCursor(
        drive: String,
        serverId: String,
        result: MatrixCursor
    ) {
        result.newRow().apply {

            add(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                serverId + DOCUMENT_ID_SEPARATOR + drive
            )
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, drive)
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)
            add(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.MIME_TYPE_DIR
            )
            add(DocumentsContract.Document.COLUMN_SIZE, null)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null)
        }
    }

    /**
     * Adds a device as a root node in the SAF tree
     */
    private fun addDeviceToCursor(
        pairedDevice: PairedDevice,
        cursor: MatrixCursor
    ) {
        cursor.newRow().apply {

            add(
                DocumentsContract.Root.COLUMN_ROOT_ID,
                pairedDevice.deviceInfo.id
            )
            add(
                DocumentsContract.Root.COLUMN_DOCUMENT_ID,
                pairedDevice.deviceInfo.id + DOCUMENT_ID_SEPARATOR
            ) // this is the document name that will be queried when the root is selected

            val flags = DocumentsContract.Root.FLAG_SUPPORTS_CREATE
            add(DocumentsContract.Root.COLUMN_FLAGS, flags)

            add(DocumentsContract.Root.COLUMN_ICON, R.drawable.windows_icon)

            add(
                DocumentsContract.Root.COLUMN_TITLE,
                pairedDevice.deviceInfo.name
            )
            add(
                DocumentsContract.Root.COLUMN_SUMMARY,
                pairedDevice.deviceInfo.os
            )

        }
    }

    private fun getDevicesRepository(): DevicesRepository {

        val appContext =
            context?.applicationContext ?: throw IllegalStateException()
        val accessor =
            EntryPointAccessors.fromApplication<PCDocumentsProviderEntryPoint>(
                appContext
            )

        return accessor.devicesRepository()
    }

    /**
     * Searches for a device and returns connection object if it was found
     * or null.
     *
     * This looks in the [connectionsCache] first and if it is not there, it searches
     * for the device on the network
     */
    private fun getDeviceConnection(deviceId: String): Connection? {

        if (connectionsCache.containsKey(deviceId)) return connectionsCache[deviceId]

        val deviceRepository = getDevicesRepository()

        return try {
            val pairedDevice =
                runBlocking { deviceRepository.getPairedDevice(deviceId) }

            val connection = runBlocking {
                Connectivity.findDevice(pairedDevice.deviceInfo.id)
                    ?.toConnection(pairedDevice.token, pairedDevice.certificate)
            }

            if (connection != null) connectionsCache[deviceId] = connection
            connection
        } catch (e: Exception) {
            Log.e("getDeviceConnection", e.stackTraceToString())
            null
        }
    }

    /**
     * Returns a [Pair] (serverId, filePath)
     * extracted from a document id.
     */
    private fun splitDocumentId(documentId: String): Pair<String, String> {
        val splitDocumentId = documentId.split(DOCUMENT_ID_SEPARATOR)
        val serverId = splitDocumentId[0]
        val directory = splitDocumentId[1]

        return serverId to directory
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PCDocumentsProviderEntryPoint {
        fun devicesRepository(): DevicesRepository
    }

    companion object {

        private const val DOCUMENT_ID_SEPARATOR = "*"

        private val DEFAULT_ROOT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
        )

        private val DEFAULT_DOCUMENT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE
        )
    }
}