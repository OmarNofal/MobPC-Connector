package com.omar.pcconnector.network.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.omar.pcconnector.model.DirectoryResource
import com.omar.pcconnector.model.FileResource
import com.omar.pcconnector.model.Resource
import com.omar.pcconnector.network.exceptions.ErrorCodes
import com.omar.pcconnector.network.exceptions.throwException
import java.nio.file.Path


@Keep
data class GeneralResponse<T>(

    @SerializedName("result")
    val result: String,

    @SerializedName("code")
    val errorCode: Int?,

    @SerializedName("message")
    val errorMessage: String?,

    @SerializedName("data")
    val data: T?

)

fun <T> GeneralResponse<T>?.getDataOrThrow(): T? {
    if (this == null) throwException(ErrorCodes.UNKNOWN_ERROR)
    if (result == "ok")
        return data
    else throwException(errorCode, errorMessage ?: "No error message from the server")
}


/**
 * Represents a file or directory returned from the network
 *
 * Note that the network most probably won't return the list of [resources]
 * of directories for performance reasons but it is there anyways
 */
@Keep
data class NetworkResource(
    @SerializedName("name")
    val name: String,

    @SerializedName("size")
    val size: Long,

    @SerializedName("creationDate")
    val creationTimeMs: Long,

    @SerializedName("lastModificationDate")
    val modificationTimeMs: Long,

    @SerializedName("type")
    val type: String,

    @SerializedName("numberOfResources")
    val numberOfResources: Int?,

    @SerializedName("content")
    val resources: List<NetworkResource>?
)

@Keep
data class VerifyTokenResponse(
    @SerializedName("valid")
    val isVerified: Boolean
)

@Keep
data class LoginResponse(
    @SerializedName("token")
    val token: Token
)

@Keep
data class FileAccessTokenResponse(
    @SerializedName("token")
    val token: Token
)

fun NetworkResource.toDomainResource(path: Path): Resource {

    val resource: Resource = if (type.lowercase() == "file") {
        FileResource(name, size, creationTimeMs, modificationTimeMs, path.resolve(name))
    } else if (type.lowercase() == "directory") {
        val directoryPath = path.resolve(name)
        DirectoryResource(
            name,
            size,
            creationTimeMs,
            modificationTimeMs,
            directoryPath,
            resources?.map { it.toDomainResource(directoryPath) } ?: listOf(),
            numberOfResources ?: -1
        )
    } else
        throwException(
            ErrorCodes.INVALID_RESPONSE,
            "The network resource $this contains invalid data"
        )

    return resource
}