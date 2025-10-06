package com.spliteasy.spliteasy.domain.repository

import com.spliteasy.spliteasy.domain.model.HouseholdMini

interface RepresentativeRepository {
    suspend fun meId(): Result<Long>
    suspend fun listAllHouseholds(): Result<List<HouseholdMini>>
}
