package com.spliteasy.spliteasy.data.remote.api

data class ProfileDto(
    val id: Long?,
    val username: String?,
    val email: String?,
    val roles: List<String>?
)
