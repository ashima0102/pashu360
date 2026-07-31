package com.pashu360.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.animal.data.local.AnimalEntity
import com.pashu360.app.feature.milk.data.local.MilkRecordDao
import com.pashu360.app.feature.milk.data.local.MilkRecordEntity

@Database(
    entities = [
        AnimalEntity::class,
        MilkRecordEntity::class,
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun milkRecordDao(): MilkRecordDao

    companion object {
        const val DATABASE_NAME = "pashu360.db"
    }
}
