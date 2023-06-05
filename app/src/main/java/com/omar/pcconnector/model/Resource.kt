package com.omar.pcconnector.model



abstract class Resource(
    open val name: String,
    open val size: Long,
    open val creationDateMs: Long,
    open val modificationDateMs: Long
)


data class FileResource(
    override val name: String,
    override val size: Long,
    override val creationDateMs: Long,
    override val modificationDateMs: Long,

): Resource(name, size, creationDateMs, modificationDateMs)


data class DirectoryResource(
    override val name: String,
    override val size: Long,
    override val creationDateMs: Long,
    override val modificationDateMs: Long,
    val resources: List<Resource>,
    val numResources: Int
): Resource(name, size, creationDateMs, modificationDateMs) {

}