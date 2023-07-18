package com.omar.pcconnector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.omar.pcconnector.network.connection.Connection
import com.omar.pcconnector.ui.detection.DetectionScreen
import com.omar.pcconnector.ui.event.ApplicationEvent
import com.omar.pcconnector.ui.session.ServerSession
import com.omar.pcconnector.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var eventsFlow: MutableSharedFlow<ApplicationEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var currentConnection by  mutableStateOf<Connection?>(null)

        setContent {

            AppTheme {
                if (currentConnection == null)
                    DetectionScreen(onConnectionSelected = { currentConnection = it })
                else
                    ServerSession(modifier = Modifier.fillMaxSize(), currentConnection!!, eventsFlow)
            }
        }
    }


}
