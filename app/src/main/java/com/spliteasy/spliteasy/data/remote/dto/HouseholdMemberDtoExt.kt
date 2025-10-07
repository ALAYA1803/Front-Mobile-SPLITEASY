package com.spliteasy.spliteasy.data.remote.dto

fun HouseholdMemberDto.normalizedHouseholdId(): Long? {

    return this.householdId
        ?: this.household_id
        ?: this.household?.id
}
