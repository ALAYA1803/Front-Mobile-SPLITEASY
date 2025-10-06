package com.spliteasy.spliteasy.data.remote.dto

/**
 * Devuelve el householdId sin importar si viene en camelCase, snake_case
 * o anidado dentro de un objeto household.
 */
fun HouseholdMemberDto.normalizedHouseholdId(): Long? {
    // Ajusta los nombres si tu DTO usa otros campos
    return this.householdId
        ?: this.household_id
        ?: this.household?.id
}
