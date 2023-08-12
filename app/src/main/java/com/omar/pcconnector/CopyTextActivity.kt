package com.omar.pcconnector

import android.os.Bundle
import com.omar.pcconnector.worker.ProcessTextWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CopyTextActivity : BaseProcessTextActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        action = ProcessTextWorker.ACTION_COPY
        super.onCreate(savedInstanceState)
    }

}