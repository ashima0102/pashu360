package com.pashu360.app.di

import android.content.Context
import androidx.room.Room
import com.pashu360.app.core.data.local.AppDatabase
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.breeding.data.local.BreedingRecordDao
import com.pashu360.app.feature.breeding.data.local.HeatRecordDao
import com.pashu360.app.feature.breeding.data.local.PregnancyRecordDao
import com.pashu360.app.feature.farm.data.local.FarmDao
import com.pashu360.app.feature.feeding.data.local.FeedInventoryDao
import com.pashu360.app.feature.feeding.data.local.FeedRecordDao
import com.pashu360.app.feature.feeding.data.local.FeedTypeDao
import com.pashu360.app.feature.finance.data.local.FinancialRecordDao
import com.pashu360.app.feature.health.data.local.HealthRecordDao
import com.pashu360.app.feature.health.data.local.VaccinationDao
import com.pashu360.app.feature.health.data.local.VetContactDao
import com.pashu360.app.feature.milk.data.local.MilkRecordDao
import com.pashu360.app.feature.notifications.data.local.AlertDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAnimalDao(db: AppDatabase): AnimalDao = db.animalDao()

    @Provides
    fun provideMilkRecordDao(db: AppDatabase): MilkRecordDao = db.milkRecordDao()

    @Provides
    fun provideHealthRecordDao(db: AppDatabase): HealthRecordDao = db.healthRecordDao()

    @Provides
    fun provideVaccinationDao(db: AppDatabase): VaccinationDao = db.vaccinationDao()

    @Provides
    fun provideVetContactDao(db: AppDatabase): VetContactDao = db.vetContactDao()

    @Provides
    fun provideFinancialRecordDao(db: AppDatabase): FinancialRecordDao =
        db.financialRecordDao()

    @Provides
    fun provideAlertDao(db: AppDatabase): AlertDao = db.alertDao()

    @Provides
    fun provideFeedTypeDao(db: AppDatabase): FeedTypeDao = db.feedTypeDao()

    @Provides
    fun provideFeedRecordDao(db: AppDatabase): FeedRecordDao = db.feedRecordDao()

    @Provides
    fun provideFeedInventoryDao(db: AppDatabase): FeedInventoryDao = db.feedInventoryDao()

    @Provides
    fun provideHeatRecordDao(db: AppDatabase): HeatRecordDao = db.heatRecordDao()

    @Provides
    fun provideBreedingRecordDao(db: AppDatabase): BreedingRecordDao = db.breedingRecordDao()

    @Provides
    fun providePregnancyRecordDao(db: AppDatabase): PregnancyRecordDao = db.pregnancyRecordDao()

    @Provides
    fun provideFarmDao(db: AppDatabase): FarmDao = db.farmDao()
}
