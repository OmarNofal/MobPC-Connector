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

    suspend fun getAllPairedDevices() = withContext(Dispatchers.IO) {
        deviceDao.getAllDevices().map { it.toPairedDevice() }
    }

    fun getPairedDeviceFlow(id: String) =
        deviceDao.getDeviceFlow(id).map { it.toPairedDevice() }

    fun getPairedDevicesFlow() = deviceDao.getDevicesFlow()
        .map { it.map { deviceEntity -> deviceEntity.toPairedDevice() } }

    suspend fun getPairedDevice(id: String) = withContext(Dispatchers.IO) {
        deviceDao.getDevice(id).toPairedDevice()
    }

    fun deleteDevice(serverId: String) =
        scope.launch { deviceDao.deleteById(serverId) }

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
            this.certificate,
            this.deviceInfo.os,
            this.autoConnect
        )


    private fun DeviceEntity.toPairedDevice() =
        PairedDevice(
            deviceInfo = DeviceInfo(id, name, os),
            token = token,
            autoConnect = autoConnect,
            certificate = certificate
        )
}