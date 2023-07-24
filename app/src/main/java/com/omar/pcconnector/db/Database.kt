package com.omar.pcconnector.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [WorkerEntity::class], version = 2, exportSchema = false)
abstract class Database: RoomDatabase() {
    abstract fun workerDao(): WorkerDao
}