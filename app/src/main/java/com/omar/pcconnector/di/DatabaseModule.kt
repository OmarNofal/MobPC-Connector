package com.omar.pcconnector.di

import android.content.Context
import androidx.room.Room
import com.omar.pcconnector.db.Database
import com.omar.pcconnector.db.WorkerDao
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Singleton
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): Database {
        return Room.databaseBuilder(
            context, Database::class.java, "Database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideWorkerDao(
        database: Database
    ): WorkerDao {
        return database.workerDao()
    }

}