package com.omar.pcconnector

import android.content.Intent
import android.os.Bundle
import com.omar.pcconnector.worker.ProcessTextWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CopyTextActivity : BaseProcessTextActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        action = ProcessTextWorker.ACTION_COPY
        data = when(intent.action) {
            Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            else -> ""
        }
        super.onCreate(savedInstanceState)
    }

}