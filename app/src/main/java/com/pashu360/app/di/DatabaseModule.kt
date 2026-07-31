package com.pashu360.app.di

import android.content.Context
import androidx.room.Room
import com.pashu360.app.core.data.local.AppDatabase
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.finance.data.local.FinancialRecordDao
import com.pashu360.app.feature.health.data.local.HealthRecordDao
import com.pashu360.app.feature.health.data.local.VaccinationDao
import com.pashu360.app.feature.health.data.local.VetContactDao
import com.pashu360.app.feature.milk.data.local.MilkRecordDao
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
}
