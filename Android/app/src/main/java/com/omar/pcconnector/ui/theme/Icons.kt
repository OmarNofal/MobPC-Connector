package com.omar.pcconnector.ui.theme

import com.omar.pcconnector.R


fun iconForExtension(extension: String) = when(extension) {

    /* Images, Videos & Audio */
    "jpg", "jpeg", "png", "gif", "tiff", "webp", "bmp" -> R.drawable.image
    "mp4", "mkv", "wmv", "avi", "flv" -> R.drawable.video
    "mp3", "m4a", "flac", "wav", "ogg", "aiff", "aac" -> R.drawable.audio

    /* Programming Languages */
    "go" -> R.drawable.go
    "rs" -> R.drawable.rust
    "js", "ts" -> R.drawable.js
    "kt" -> R.drawable.kotlin
    "java" -> R.drawable.java
    "php" -> R.drawable.php
    "c", "cc" -> R.drawable.c
    "cs" -> R.drawable.c_
    "cpp", "hpp" -> R.drawable.cpp
    "rb" -> R.drawable.ruby
    "html" -> R.drawable.html
    "py" -> R.drawable.python
    "hs" -> R.drawable.haskell
    "powershell", "bash", "sh", "csh" -> R.drawable.script


    /* Microsoft Stuff */
    "doc", "docx" -> R.drawable.msword
    "xls", "xlsx" -> R.drawable.excel
    "one" -> R.drawable.onenote
    "ppt", "pptx", "pptm" -> R.drawable.powerpoint
    "exe" -> R.drawable.exe
    "dll" -> R.drawable.dll

    /* Adobe Stuff */
    "pdf" -> R.drawable.pdf
    "ai" -> R.drawable.ai
    "psd" -> R.drawable.ps
    "aep" -> R.drawable.ae
    "xd" -> R.drawable.xd
    "indd" -> R.drawable.id


    /* Archived files */
    "zip", "rar", "jar", "lib", "7z", "tar", "a", "ar", "iso" -> R.drawable.compressed

    "epub" -> R.drawable.epub

    else -> R.drawable.text
}