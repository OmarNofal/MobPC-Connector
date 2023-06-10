package com.omar.pcconnector

import java.nio.file.Path
import kotlin.io.path.absolutePathString


val Path.absolutePath: String
    get() = absolutePathString().removePrefix("/")