package com.omar.pcconnector.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("SELECT * FROM DeviceEntity")
    suspend fun getAllDevices(): List<DeviceEntity>

    @Query("SELECT COUNT(*) > 1 FROM DeviceEntity WHERE id = :id LIMIT 1")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM DeviceEntity WHERE id = :id")
    fun getDeviceFlow(id: String): Flow<DeviceEntity>

}