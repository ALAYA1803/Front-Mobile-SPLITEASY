package com.spliteasy.spliteasy.data.remote.dto

data class MemberDto(
    val id: Long,
    val householdId: Long? = null,
    val household_id: Long? = null,
    val household: NestedHousehold? = null,
    val userId: Long? = null,
    val name: String? = null,
    val role: String? = null,
) {
    data class NestedHousehold(val id: Long?)
    fun normalizedHouseholdId(): Long? = householdId ?: household_id ?: household?.id
}
