package com.omar.pcconnector.di

import android.content.Context
import android.content.SharedPreferences
import com.omar.pcconnector.network.connection.AppConnectivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton



private const val SHARED_PREFS_NAME = "NETWORK"


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ConnectivitySharedPreferences


@Module
@InstallIn(SingletonComponent::class)
object ConnectivityModule {


    @Provides
    @Singleton
    fun provideAppConnectivity(
        @ConnectivitySharedPreferences prefs: SharedPreferences
    ): AppConnectivity {
        return AppConnectivity(prefs)
    }


    @Provides
    @ConnectivitySharedPreferences
    fun providesConnectivitySharedPreferences(
        @ApplicationContext applicationContext: Context
    ): SharedPreferences {
        return applicationContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    fun provideRetrofit(connectivity: AppConnectivity): Retrofit? {
        return connectivity.currentConnection.value?.retrofit
    }

}
