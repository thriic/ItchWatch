package com.thriic.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.thriic.core.local.AppDatabase
import com.thriic.core.local.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DIModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
    //OkHttpClient.Builder()
    //        .cache(Cache(File(context.cacheDir, "http_cache"), (20 * 1024 * 1024).toLong()))
    //        .build()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "iw-database"
    ).build()

    @Provides
    @Singleton
    fun providePreferences(
        @ApplicationContext context: Context
    ) : UserPreferences = UserPreferences(context)

    @Provides
    @Singleton
    fun provideGameDao(appDatabase: AppDatabase) = appDatabase.gameDao()

    @Provides
    @Singleton
    fun provideInfoDao(appDatabase: AppDatabase) = appDatabase.infoDao()

    @Provides
    @Singleton
    fun provideTagDao(appDatabase: AppDatabase) = appDatabase.tagDao()

}

