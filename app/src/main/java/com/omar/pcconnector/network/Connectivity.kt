//package com.omar.pcconnector.network
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.ConnectivityManager.NetworkCallback
//import android.net.Network
//import android.net.NetworkCapabilities
//import android.net.NetworkRequest
//import android.util.Log
//import androidx.core.content.getSystemService
//import com.omar.pcconnector.Api
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.launch
//import retrofit2.Retrofit
//import kotlin.math.pow
//
//class Connectivity(private val appContext: Context) {
//
//
//    init {
//        registerForCallback()
//    }
//
//
//    private fun registerForCallback() {
//
//        val connectivityManager = appContext.getSystemService<ConnectivityManager>()
//            ?: throw Exception("Connectivity Manager is not available. The app will terminate")
//
//
//        // we need wifi only
//        val networkRequest = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
//
//        val networkCallback = object : NetworkCallback() {
//            override fun onAvailable(network: Network) {
//                super.onAvailable(network)
//
//                val linkInfo = connectivityManager.getLinkProperties(network)!!
//                val ipv4AddressString = linkInfo.linkAddresses[1].address.toString().split('/')[1]
//                Log.i("INET", ipv4AddressString)
//
//                val ipAddress = ipAddressStringToInt(ipv4AddressString)
//
//                Log.i("NET", "Wifi Connection obtained $ipAddress")
//                performPingSweep(ipAddress, linkInfo.linkAddresses[1].prefixLength)
//            }
//
//            override fun onLost(network: Network) {
//                super.onLost(network)
//
//                Log.i("NET", "Wifi connection lost")
//                // TODO set ip address to null
//            }
//        }
//
//        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
//    }
//
//
//
//    fun performPingSweep(ipAddress: Int, subnetMask: Int) {
//
//        val coroutineScope = CoroutineScope(Dispatchers.IO)
//
//        for (add in getIPAddressesIterator(ipAddress, subnetMask)) {
//            val retrofit = Retrofit.Builder().baseUrl("http://${ipAddressToString(add)}:6543/")
//                .build()
//
//            val api = retrofit.create(Api::class.java)
//
//            coroutineScope.launch {
//                try {
//                    Log.i("TEST", "TESTING $add")
//                    val response = api.status().execute()
//                    Log.i("RES", response.body().toString())
//                } catch (e: Exception) {
//                    Log.e("TEST", "$add: NO RESPONSE ${e.message}")
//                    cancel()
//                }
//            }
//        }
//    }
//
//}