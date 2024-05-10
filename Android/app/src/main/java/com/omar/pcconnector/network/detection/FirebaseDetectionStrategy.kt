package com.omar.pcconnector.network.detection

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.omar.pcconnector.network.api.secureClient
import com.omar.pcconnector.operation.StatusOperation
import com.omar.pcconnector.statusApi
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * When the computer is not available on the local network
 * we use firebase to try and find its global ip address
 * so the user can access it from anywhere, not restricted
 * to his house.
 */
object FirebaseDetectionStrategy : DeviceFinder {

    private val firebaseDb = FirebaseDatabase.getInstance()

    override suspend fun findDevice(uuid: String): DetectedHost? {
        val ref = firebaseDb.getReference(uuid)
        Log.d("Firebase", "Locating device")
        try {
            val deviceInfo = ref.get().await() ?: return null
            val ip = deviceInfo.child("ip").getValue(String::class.java) ?: return null
            val port = deviceInfo.child("port").getValue(Int::class.java) ?: return null
            val api = Retrofit.Builder().client(secureClient.build()).baseUrl("https://$ip:$port")
                .addConverterFactory(GsonConverterFactory.create())
                .build().statusApi()
            val hostInfo = StatusOperation(api).start()
            return DetectedHost(uuid, hostInfo.name, hostInfo.os, ip, port)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Failed to find the device\n ${e.stackTraceToString()}")
            return null
        }
    }

}