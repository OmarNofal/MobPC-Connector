package com.omar.pcconnector

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.math.pow


val Path.absolutePath: String
    get() = absolutePathString().removePrefix("/")



val sizeRanges = arrayOf(
    2.0.pow(10.0).toLong() until 2.0.pow(20.0).toLong() to "KB",
    2.0.pow(20.0).toLong() until 2.0.pow(30.0).toLong() to "MB",
    2.0.pow(30.0).toLong() until Long.MAX_VALUE to "GB"
)

// Converts size in bytes to human-readable format
// ex 4096bytes = 4KB
fun Long.bytesToSizeString(): String {

    if (this in 0 until 1024) return "$this Bytes"

    val result = try {
        val sizeRange = sizeRanges.first { this in it.first }
        "${this / sizeRange.first.first} ${sizeRange.second}"
    } catch (e: NoSuchElementException) {
        "Unknown size"
    }

    return result
}