package com.omar.pcconnector.pairing

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class PairingPayload(

    @SerializedName("name")
    val serverName: String,

    @SerializedName("uuid")
    val serverId: String,

    @SerializedName("os")
    val os: String,

    @SerializedName("ipAddresses")
    val ipAddresses: List<String>,

    @SerializedName("cert")
    val certificate: String,

    @SerializedName("pairingToken")
    val pairingToken: String,

    @SerializedName("port")
    val port: Int

)