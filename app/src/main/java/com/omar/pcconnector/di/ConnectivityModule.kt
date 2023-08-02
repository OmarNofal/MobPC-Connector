package com.omar.pcconnector.di

import com.omar.pcconnector.network.connection.Connectivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton




@Module
@InstallIn(SingletonComponent::class)
class ConnectivityModule {

    @Provides
    @Singleton
    fun provideAppConnectivity(): Connectivity {
        return Connectivity
    }

}
