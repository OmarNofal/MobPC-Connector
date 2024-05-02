package com.omar.pcconnector.security

import android.content.Context
import android.security.KeyChain


fun Context.trustCertificate(cert: String) {

    val bytes = cert.toByteArray()

    val intent = KeyChain.createInstallIntent()
    intent.apply {
        //putExtra(KeyChain.EXTRA_CERTIFICATE, bytes)
        //putExtra(KeyChain.EXTRA_NAME, "Omar's Server")
    }

    startActivity(intent)
}
