package com.omar.pcconnector.network.api

import android.annotation.SuppressLint
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager


/**
 * This class implements a trust manager which uses
 * the default trust manager as well as a custom trust manager
 * supplied in the constructor
 */
@SuppressLint("CustomX509TrustManager")
class CompositeTrustManager(
    private val defaultTrustManager: X509TrustManager,
    private val customTrustManager: X509TrustManager
) : X509TrustManager {


    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) {
        defaultTrustManager.checkClientTrusted(chain, authType);
    }

    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) {
        try {
            customTrustManager.checkServerTrusted(chain, authType)
        } catch (e: CertificateException) {
            defaultTrustManager.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {

        val certificates = mutableListOf<X509Certificate>()
        certificates.addAll(defaultTrustManager.acceptedIssuers)
        certificates.addAll(customTrustManager.acceptedIssuers)

        return certificates.toTypedArray()
    }

}