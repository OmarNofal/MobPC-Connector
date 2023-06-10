package com.omar.pcconnector.operation.transfer


import android.content.Context
import androidx.documentfile.provider.DocumentFile
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.worker.DownloadWorker
import com.omar.pcconnector.worker.UploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.file.Path
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.absolutePathString


/**
 * This is a singleton class which is a single source of truth for all
 * download and upload operations. It is responsible for starting, canceling and monitoring all transfers
 */
@Singleton
class TransfersManager @Inject constructor(
    @ApplicationContext private val context: Context
) {


    fun download(
        connection: Connection,
        pathOnServer: Path,
        downloadPath: DocumentFile
    ) {

        val downloadWorkerRequest =
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(UUID.randomUUID())
                .setInputData(
                    workDataOf(
                        "pathOnServer" to pathOnServer.absolutePathString(),
                        "api_endpoint" to connection.retrofit.baseUrl().toString(),
                        "downloadUri" to downloadPath.uri.toString(),
                    )
                ).build()

        WorkManager.getInstance(context).enqueue(downloadWorkerRequest)
    }


    fun upload(
        files: List<DocumentFile>,
        connection: Connection,
        path: Path
    ) {
        val uploadWorkerRequest =
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(UUID.randomUUID())
                .setInputData(
                    workDataOf(
                        "uris" to files.map { it.uri.toString() }.toTypedArray(),
                        "api_endpoint" to connection.retrofit.baseUrl().toString(),
                        "path" to path.absolutePathString(),
                        "directory_flags" to files.toDirectoryFlags().toTypedArray()
                    )
                ).build()

        WorkManager.getInstance(context).enqueue(uploadWorkerRequest)
    }

    private fun List<DocumentFile>.toDirectoryFlags(): List<Boolean> = map { it.isDirectory }
}