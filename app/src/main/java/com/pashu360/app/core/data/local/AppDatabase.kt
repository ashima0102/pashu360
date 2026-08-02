package com.pashu360.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import com.pashu360.app.feature.feeding.data.local.FeedInventoryDao
import com.pashu360.app.feature.feeding.data.local.FeedInventoryEntity
import com.pashu360.app.feature.feeding.data.local.FeedRecordDao
import com.pashu360.app.feature.feeding.data.local.FeedRecordEntity
import com.pashu360.app.feature.feeding.data.local.FeedTypeDao
import com.pashu360.app.feature.feeding.data.local.FeedTypeEntity
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
import com.pashu360.app.feature.notifications.data.local.AlertDao
import com.pashu360.app.feature.notifications.data.local.AlertEntity

@Database(
    entities = [
        AnimalEntity::class,
        MilkRecordEntity::class,
        HealthRecordEntity::class,
        VaccinationEntity::class,
        VetContactEntity::class,
        FinancialRecordEntity::class,
        AlertEntity::class,
        FeedTypeEntity::class,
        FeedRecordEntity::class,
        FeedInventoryEntity::class,
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun milkRecordDao(): MilkRecordDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun vetContactDao(): VetContactDao
    abstract fun financialRecordDao(): FinancialRecordDao
    abstract fun alertDao(): AlertDao
    abstract fun feedTypeDao(): FeedTypeDao
    abstract fun feedRecordDao(): FeedRecordDao
    abstract fun feedInventoryDao(): FeedInventoryDao

    companion object {
        const val DATABASE_NAME = "pashu360.db"
    }
}
