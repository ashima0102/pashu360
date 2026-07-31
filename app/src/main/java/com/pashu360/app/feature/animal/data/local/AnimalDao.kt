package com.pashu360.app.feature.animal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {

    @Query("""
        SELECT * FROM animals
        WHERE farm_id = :farmId
        AND status NOT IN ('sold', 'deceased')
        ORDER BY tag_id ASC
    """)
    fun observeActiveAnimals(farmId: String): Flow<List<AnimalEntity>>

    @Query("""
        SELECT * FROM animals
        WHERE farm_id = :farmId
        AND status = :status
        ORDER BY tag_id ASC
    """)
    fun observeByStatus(farmId: String, status: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE farm_id = :farmId ORDER BY tag_id ASC")
    fun observeAll(farmId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AnimalEntity?>

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND tag_id = :tagId LIMIT 1")
    suspend fun getByTag(farmId: String, tagId: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE farm_id = :farmId AND qr_code_data = :qrData LIMIT 1")
    suspend fun getByQr(farmId: String, qrData: String): AnimalEntity?

    @Query("""
        SELECT * FROM animals
        WHERE farm_id = :farmId
        AND (name LIKE '%' || :query || '%' OR tag_id LIKE '%' || :query || '%')
        ORDER BY tag_id
    """)
    fun search(farmId: String, query: String): Flow<List<AnimalEntity>>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'active'")
    fun countActive(farmId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'sick'")
    fun countSick(farmId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM animals WHERE farm_id = :farmId AND status = 'pregnant'")
    fun countPregnant(farmId: String): Flow<Int>

    // NOTE: Explicit Long/Int return types are required — KSP2 has a bug where
    // suspend functions returning Unit (V in JVM bytecode) fail with
    // "unexpected jvm signature V". See https://github.com/google/ksp/issues/2957
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(animal: AnimalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<AnimalEntity>): List<Long>

    @Update
    suspend fun update(animal: AnimalEntity): Int

    @Query("UPDATE animals SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: String): Int

    @Query("DELETE FROM animals WHERE id = :id")
    suspend fun delete(id: String): Int
}
