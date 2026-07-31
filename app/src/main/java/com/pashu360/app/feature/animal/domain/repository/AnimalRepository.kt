package com.pashu360.app.feature.animal.domain.repository

import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import kotlinx.coroutines.flow.Flow

interface AnimalRepository {
    fun observeAnimals(farmId: String, filter: AnimalFilter): Flow<List<Animal>>
    fun observeAnimalById(id: String): Flow<Animal?>
    fun searchAnimals(farmId: String, query: String): Flow<List<Animal>>
    fun countActive(farmId: String): Flow<Int>
    fun countSick(farmId: String): Flow<Int>
    fun countPregnant(farmId: String): Flow<Int>

    suspend fun getAnimalById(id: String): Animal?
    suspend fun getAnimalByTag(farmId: String, tagId: String): Animal?
    suspend fun getAnimalByQr(farmId: String, qrData: String): Animal?
    suspend fun addAnimal(animal: Animal): Result<Unit>
    suspend fun updateAnimal(animal: Animal): Result<Unit>
    suspend fun updateStatus(id: String, status: String): Result<Unit>
    suspend fun deleteAnimal(id: String): Result<Unit>
}
