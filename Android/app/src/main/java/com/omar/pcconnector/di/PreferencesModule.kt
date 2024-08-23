package com.omar.pcconnector.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.omar.pcconnector.preferences.UserPreferences
import com.omar.pcconnector.preferences.userPreferencesDatastore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
object PreferencesModule {


    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<UserPreferences> = context.userPreferencesDatastore



}