package com.omar.pcconnector.network.exceptions


abstract class NetworkException(msg: String = "Network error"): RuntimeException(msg)

class UnknownNetworkException: NetworkException("Unknown exception")
class InvalidRequestException(msg: String): NetworkException(msg)
class ResourceDoesNotExistException(msg: String): NetworkException(msg)
class PermissionDeniedException(msg: String): NetworkException(msg)
class InvalidResponseException(msg: String): NetworkException(msg)

object ErrorCodes {
    const val INVALID_REQUEST   =         1
    const val PERMISSION_DENIED =         2
    const val DOES_NOT_EXIST    =         3
    const val UNKNOWN_ERROR     =         4
    const val INVALID_RESPONSE  =         5
}

fun throwException(errorCode: Int?, msg: String = "An unknown error occurred"): Nothing {
    throw when (errorCode) {
        ErrorCodes.UNKNOWN_ERROR -> UnknownNetworkException()
        ErrorCodes.INVALID_REQUEST -> InvalidRequestException(msg)
        ErrorCodes.DOES_NOT_EXIST -> ResourceDoesNotExistException(msg)
        ErrorCodes.PERMISSION_DENIED -> PermissionDeniedException(msg)
        ErrorCodes.INVALID_RESPONSE -> InvalidResponseException(msg)
        else -> UnknownNetworkException()
    }
}
