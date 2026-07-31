package com.pashu360.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pashu360.app.feature.animal.data.local.AnimalDao
import com.pashu360.app.feature.animal.data.local.AnimalEntity

@Database(
    entities = [
        AnimalEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao

    companion object {
        const val DATABASE_NAME = "pashu360.db"
    }
}
