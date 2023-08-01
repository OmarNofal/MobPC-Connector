package com.omar.pcconnector.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DeviceDao {

    @Insert
    suspend fun insert(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("SELECT * FROM DeviceEntity")
    suspend fun getAllDevices(): List<DeviceEntity>

    @Query("SELECT COUNT(*) > 1 FROM DeviceEntity WHERE id = :id LIMIT 1")
    suspend fun exists(id: String): Boolean

}