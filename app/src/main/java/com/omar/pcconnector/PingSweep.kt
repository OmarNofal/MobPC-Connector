package com.omar.pcconnector

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter.formatIpAddress
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import java.util.*


class PingSweep {



    fun pingSweep(
        appContext: Context,
        onDetected: () -> Unit,
        onNotDetected: () -> Unit) {

        val context = appContext
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager


        Log.i("Device Info", "This device's IP Address is ${wm.connectionInfo.ipAddress}")

    }



}