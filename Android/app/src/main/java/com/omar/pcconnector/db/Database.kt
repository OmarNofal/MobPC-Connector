package com.omar.pcconnector.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [WorkerEntity::class, DeviceEntity::class], version = 1, exportSchema = false)
abstract class Database: RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun devicesDao(): DeviceDao
}