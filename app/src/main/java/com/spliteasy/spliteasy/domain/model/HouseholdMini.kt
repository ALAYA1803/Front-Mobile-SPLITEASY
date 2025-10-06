package com.spliteasy.spliteasy.domain.model

data class HouseholdMini(
    val id: Long,
    val name: String,
    val description: String,
    val currency: String,
    val representanteId: Long
)