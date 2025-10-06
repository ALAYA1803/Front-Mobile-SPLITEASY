package com.spliteasy.spliteasy.domain.repository

import com.spliteasy.spliteasy.domain.model.HouseholdMini
import com.spliteasy.spliteasy.data.remote.dto.HouseholdDto
interface HouseholdRepository {
    suspend fun getHouseholdByRepresentative(representativeId: Long): Result<HouseholdMini?>
    suspend fun createHousehold(
        name: String,
        description: String,
        currency: String,
        representativeId: Long
    ): Result<HouseholdMini>
}
