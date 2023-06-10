package com.omar.pcconnector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.omar.pcconnector.ui.detection.DetectionScreen
import com.omar.pcconnector.ui.main.MainApp
import com.omar.pcconnector.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            AppTheme {
                var isDetectionScreen by rememberSaveable { mutableStateOf(true) }
                if (isDetectionScreen)
                    DetectionScreen(onSwitchScreen = { isDetectionScreen = false })
                else
                    MainApp()
            }
        }
    }


}
