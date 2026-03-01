package com.fraud_detector.di

import android.content.Context
import com.fraud_detector.database.AppDatabase
import com.fraud_detector.database.ThreatDao
import com.fraud_detector.ml.TFLiteModelManager
import com.fraud_detector.utils.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.build(ctx)

    @Provides
    @Singleton
    fun provideThreatDao(db: AppDatabase): ThreatDao = db.threatDao()

    @Provides
    @Singleton
    fun provideTFLiteModelManager(@ApplicationContext ctx: Context): TFLiteModelManager =
        TFLiteModelManager(ctx)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext ctx: Context): NotificationHelper =
        NotificationHelper(ctx)
}