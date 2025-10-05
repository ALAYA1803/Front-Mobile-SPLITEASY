package com.spliteasy.spliteasy.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long,
    val username: String,
    val email: String? = null,
    val income: Double? = null,
    val roles: List<String> = emptyList()
)
