package com.omar.pcconnector.model

import java.nio.file.Path


abstract class Resource(
    open val name: String,
    open val size: Long,
    open val creationDateMs: Long,
    open val modificationDateMs: Long,
    open val path: Path
) {
    val parentPath get() = path.parent
}


data class FileResource(
    override val name: String,
    override val size: Long,
    override val creationDateMs: Long,
    override val modificationDateMs: Long,
    override val path: Path
): Resource(name, size, creationDateMs, modificationDateMs, path)


data class DirectoryResource(
    override val name: String,
    override val size: Long,
    override val creationDateMs: Long,
    override val modificationDateMs: Long,
    override val path: Path,
    val resources: List<Resource>,
    val numResources: Int
): Resource(name, size, creationDateMs, modificationDateMs, path) {

}