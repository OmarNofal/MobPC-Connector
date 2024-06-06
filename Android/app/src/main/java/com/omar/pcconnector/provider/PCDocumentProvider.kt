package com.omar.pcconnector.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import com.omar.pcconnector.R
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.model.PairedDevice
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking


class PCDocumentProvider : DocumentsProvider() {


    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PCDocumentsProviderEntryPoint {
        fun devicesRepository(): DevicesRepository
    }


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
        TODO("Not yet implemented")
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        TODO("Not yet implemented")
    }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        TODO("Not yet implemented")
    }

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

    private fun addDeviceToCursor(pairedDevice: PairedDevice, cursor: MatrixCursor) {
        cursor.newRow().apply {

            add(DocumentsContract.Root.COLUMN_ROOT_ID, pairedDevice.deviceInfo.id)
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, pairedDevice.deviceInfo.id)

            val flags = DocumentsContract.Root.FLAG_SUPPORTS_CREATE
            add(DocumentsContract.Root.COLUMN_FLAGS, flags)

            add(DocumentsContract.Root.COLUMN_ICON, R.drawable.windows_icon)

            add(DocumentsContract.Root.COLUMN_TITLE, pairedDevice.deviceInfo.name)
            add(DocumentsContract.Root.COLUMN_SUMMARY, pairedDevice.deviceInfo.os)

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

}