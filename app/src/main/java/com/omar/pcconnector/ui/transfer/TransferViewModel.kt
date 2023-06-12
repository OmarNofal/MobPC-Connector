package com.omar.pcconnector.ui.transfer

import androidx.lifecycle.ViewModel
import com.omar.pcconnector.operation.transfer.TransfersManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TransferViewModel @Inject constructor(
    //@ApplicationContext private val appContext: Context,
    private val transfersManager: TransfersManager
): ViewModel() {

    fun getTransfersFlow() = transfersManager.currentTransfers

    fun cancelTransfer(id: String) = transfersManager.cancelWorker(id)

}