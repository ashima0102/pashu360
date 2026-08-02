package com.pashu360.app.feature.farm.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<FarmEntity?>

    @Query("SELECT * FROM farms WHERE id = :id LIMIT 1")
    fun getById(id: String): FarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(farm: FarmEntity): Long

    @Query("UPDATE farms SET expected_herd_size = :size WHERE id = :id")
    fun updateExpectedHerdSize(id: String, size: Int): Int
}
