package com.omar.pcconnector.network.api

import okhttp3.OkHttpClient
import okhttp3.internal.tls.OkHostnameVerifier
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


/**
 * Generates a HTTPs client which trusts everything
 * in the Android TrustManager as well as makes an exception
 * for the certificate coming from the server.
 *
 * This is done so networking calls don't throw an exception
 * due to the server using self-signed certificate.
 */
fun clientForSSLCertificate(
    cert: String,
    serverHostname: String
): OkHttpClient {

    var tmf = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm())
    // Using null here initialises the TMF with the default trust store.
    tmf.init(null as KeyStore?)

    // Get hold of the default trust manager
    var defaultTm: X509TrustManager? = null
    for (tm in tmf.trustManagers) {
        if (tm is X509TrustManager) {
            defaultTm = tm
            break
        }
    }

    val certificateFactory =
        CertificateFactory.getInstance("X.509")
    val certificate =
        certificateFactory.generateCertificate(ByteArrayInputStream(cert.toByteArray()))

    val customKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    customKeyStore.load(null)
    customKeyStore.setCertificateEntry("alias", certificate)


    tmf = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(customKeyStore)

    var myTm: X509TrustManager? = null
    for (tm in tmf.trustManagers) {
        if (tm is X509TrustManager) {
            myTm = tm
            break
        }
    }

    val compositeTrustManager = CompositeTrustManager(defaultTm!!, myTm!!)

    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(compositeTrustManager), SecureRandom())
    }

    return OkHttpClient.Builder()
        .hostnameVerifier { hostname, session ->
            hostname == serverHostname || OkHostnameVerifier.verify(
                hostname,
                session
            )
        }
        .sslSocketFactory(sslContext.socketFactory, compositeTrustManager)
        .build()
}