package com.pashu360.app.feature.animal.domain.usecase

import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.feature.animal.domain.repository.AnimalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnimalsUseCase @Inject constructor(
    private val repo: AnimalRepository
) {
    operator fun invoke(farmId: String, filter: AnimalFilter = AnimalFilter.ALL): Flow<List<Animal>> =
        repo.observeAnimals(farmId, filter)
}

class GetAnimalByIdUseCase @Inject constructor(
    private val repo: AnimalRepository
) {
    operator fun invoke(id: String): Flow<Animal?> = repo.observeAnimalById(id)
}

class SearchAnimalsUseCase @Inject constructor(
    private val repo: AnimalRepository
) {
    operator fun invoke(farmId: String, query: String): Flow<List<Animal>> =
        repo.searchAnimals(farmId, query)
}

class AddAnimalUseCase @Inject constructor(
    private val repo: AnimalRepository
) {
    suspend operator fun invoke(animal: Animal): Result<Unit> {
        // Validate before saving
        require(animal.tagId.isNotBlank()) { "Tag ID is required" }
        require(animal.tagId.length <= 20) { "Tag ID too long (max 20 characters)" }
        require(animal.tagId.matches(Regex("[a-zA-Z0-9-]+"))) {
            "Tag ID can only contain letters, numbers, and hyphens"
        }
        animal.weightKg?.let {
            require(it in 1.0..2000.0) { "Weight must be between 1 and 2000 kg" }
        }

        val existing = repo.getAnimalByTag(animal.farmId, animal.tagId)
        if (existing != null && existing.id != animal.id) {
            return Result.failure(IllegalStateException("Tag ID '${animal.tagId}' already exists"))
        }

        return repo.addAnimal(animal)
    }
}

class UpdateAnimalUseCase @Inject constructor(
    private val repo: AnimalRepository
) {
    suspend operator fun invoke(animal: Animal): Result<Unit> = repo.updateAnimal(animal)
}

class GetAnimalByQrUseCase @Inject constructor(
    private val repo: AnimalRepository
) {
    suspend operator fun invoke(farmId: String, qrData: String): Animal? =
        repo.getAnimalByQr(farmId, qrData) ?: repo.getAnimalByTag(farmId, qrData)
}

class GetAnimalStatsUseCase @Inject constructor(
    private val repo: AnimalRepository
) {
    fun activeCount(farmId: String) = repo.countActive(farmId)
    fun sickCount(farmId: String) = repo.countSick(farmId)
    fun pregnantCount(farmId: String) = repo.countPregnant(farmId)
}
