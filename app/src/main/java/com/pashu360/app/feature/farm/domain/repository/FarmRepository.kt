package com.pashu360.app.feature.farm.domain.repository

import com.pashu360.app.core.domain.model.Farm
import kotlinx.coroutines.flow.Flow

interface FarmRepository {
    fun observeFarm(farmId: String): Flow<Farm?>
    suspend fun getFarm(farmId: String): Farm?
    suspend fun saveFarm(farm: Farm): Result<Unit>
    suspend fun updateExpectedHerdSize(farmId: String, size: Int): Result<Unit>
}