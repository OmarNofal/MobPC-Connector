package com.omar.pcconnector.data

import com.omar.pcconnector.db.DeviceDao
import com.omar.pcconnector.db.DeviceEntity
import com.omar.pcconnector.model.DeviceInfo
import com.omar.pcconnector.model.PairedDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DevicesRepository @Inject constructor(
    private val deviceDao: DeviceDao
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun getAllPairedDevices() =
        deviceDao.getAllDevices().map { it.toPairedDevice() }

    fun storeDevice(
        pairedDevice: PairedDevice
    ) {
        scope.launch {
            deviceDao.insert(pairedDevice.toDeviceEntity())
        }
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