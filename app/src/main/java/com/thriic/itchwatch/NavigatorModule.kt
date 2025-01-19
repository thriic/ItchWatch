package com.thriic.itchwatch

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NavigatorModule {
    @Provides
    @Singleton
    fun provideNavigator(): Navigator =
        Navigator()
}