package com.omar.pcconnector.di

import android.content.Context
import androidx.room.Room
import com.omar.pcconnector.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): Database {
        return Room.databaseBuilder(
            context, Database::class.java, "Database"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideWorkerDao(
        database: Database
    ) = database.workerDao()

    @Singleton
    @Provides
    fun provideDeviceDao(
        database: Database
    ) = database.devicesDao()
}