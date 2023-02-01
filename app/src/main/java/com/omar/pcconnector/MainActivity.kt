package com.omar.pcconnector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.omar.pcconnector.network.detection.DetectionLocalNetworkStrategy
import com.omar.pcconnector.network.detection.DetectionStrategy
import com.omar.pcconnector.ui.theme.PCConnectorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class MainActivity : ComponentActivity() {

    //lateinit var connectivity: Connectivity

    private val retrofitClient = Retrofit.Builder()
        .baseUrl("http://172.17.83.15:6543")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofitClient.create<Api>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //connectivity = Connectivity(applicationContext)
        findHosts()
        setContent {
            PCConnectorTheme {
            }
        }
    }

    private fun findHosts() {
        val detectionString: DetectionStrategy = DetectionLocalNetworkStrategy()
        Log.i("HOSTS", "Detected Hosts: ${detectionString.getAvailableHosts()}")
    }
}
