package com.pashu360.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import com.pashu360.app.feature.finance.data.local.FinancialRecordDao
import com.pashu360.app.feature.finance.data.local.FinancialRecordEntity
import com.pashu360.app.feature.health.data.local.HealthRecordDao
import com.pashu360.app.feature.health.data.local.HealthRecordEntity
import com.pashu360.app.feature.health.data.local.VaccinationDao
import com.pashu360.app.feature.health.data.local.VaccinationEntity
import com.pashu360.app.feature.health.data.local.VetContactDao
import com.pashu360.app.feature.health.data.local.VetContactEntity
import com.pashu360.app.feature.milk.data.local.MilkRecordDao
import com.pashu360.app.feature.milk.data.local.MilkRecordEntity

@Database(
    entities = [
        AnimalEntity::class,
        MilkRecordEntity::class,
        HealthRecordEntity::class,
        VaccinationEntity::class,
        VetContactEntity::class,
        FinancialRecordEntity::class,
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun milkRecordDao(): MilkRecordDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun vetContactDao(): VetContactDao
    abstract fun financialRecordDao(): FinancialRecordDao

    companion object {
        const val DATABASE_NAME = "pashu360.db"
    }
}
