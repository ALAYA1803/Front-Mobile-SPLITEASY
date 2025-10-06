package com.spliteasy.spliteasy.data.remote.dto

data class HouseholdMemberDto(
    val id: Long,
    val userId: Long?,
    val householdId: Long?,
    val household_id: Long? = null,
    val household: HouseholdMiniDto? = null
)

data class HouseholdMiniDto(
    val id: Long?,
    val name: String? = null
)

