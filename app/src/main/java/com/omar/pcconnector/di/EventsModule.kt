package com.omar.pcconnector.di

import com.omar.pcconnector.ui.event.ApplicationEvent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class EventsModule {

    @Singleton
    @Provides
    fun provideEventsFlow(): MutableSharedFlow<ApplicationEvent> {
        return MutableSharedFlow()
    }

}
