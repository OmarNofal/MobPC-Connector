package com.omar.pcconnector.data

import com.omar.pcconnector.db.DeviceDao
import com.omar.pcconnector.db.DeviceEntity
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.model.PairedDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DevicesRepository @Inject constructor(
    private val deviceDao: DeviceDao
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun getAllPairedDevices() =
        deviceDao.getAllDevices().map { it.toPairedDevice() }

    fun getPairedDeviceFlow(id: String) =
        deviceDao.getDeviceFlow(id).map { it.toPairedDevice() }

    suspend fun getPairedDevice(id: String) = withContext(Dispatchers.IO) {
        deviceDao.getDevice(id).toPairedDevice()
    }

    suspend fun storeDevice(
        pairedDevice: PairedDevice
    ) {
        scope.launch {
            deviceDao.insert(pairedDevice.toDeviceEntity())
        }.join()
    }

    private fun PairedDevice.toDeviceEntity() =
        DeviceEntity(
            this.deviceInfo.id,
            this.deviceInfo.name,
            this.token,
            this.deviceInfo.os,
            this.autoConnect
        )


    private fun DeviceEntity.toPairedDevice() =
         PairedDevice(
             deviceInfo = DeviceInfo(id, name, os),
             token = token,
             autoConnect = autoConnect
         )
}