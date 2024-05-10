package com.omar.pcconnector.operation

import java.lang.Exception


class CreateAFileException(fileName: String): Exception("Failed to create $fileName")